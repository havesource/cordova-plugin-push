const EventEmitter = require('node:events');
const { readFile, open } = require('node:fs/promises');
const path = require('node:path');

const Platform = require('cordova-ios');
const semver = require('semver');

/**
 * Cordova does not pass "cli_variables" to the "after_plugin_install"
 * hookscripts. We will use the "nopt" package, same package used by
 * Cordova CLI, to extract the variables for this script.
 */
const nopt = require('nopt');

/**
 * Checks if the version string has a valid format.
 * SwiftPM follows a simplified version of the semver rules.
 *
 * > A package version is a three period-separated integer, for example 1.0.0.
 *
 * @see https://docs.swift.org/package-manager/PackageDescription/PackageDescription.html#version
 *
 * @param {String} version - Targeted FirebaseMessaging SDK version
 * @returns
 */
function validateVersion (version) {
  if (version === null || version === undefined) {
    return null;
  }

  if (
    typeof version === 'string' &&
    /^\d+\.\d+\.\d+$/.test(version) &&
    semver.valid(version) === version
  ) {
    return version;
  }

  return null;
}

/**
 * After the plugin is installed, attempt to update the FirebaseMessaging SDK
 * veresion in Package.swift.
 *
 * Version can come from various sources.
 *
 * The following is the order of priorty:
 * 1. CLI arguments (highest priorty)
 * 2. Project's 'package.json'
 * 3. Project's platform 'config.xml'
 * 4. Plugin's 'plugin.xml' (default value)
 *
 * @param {Object} context
 */
module.exports = async function (context) {
  const { projectRoot, plugin } = context.opts;
  const platformPath = path.join(projectRoot, 'platforms', 'ios');

  const eventEmitter = new EventEmitter();
  const iosProject = new Platform('ios', platformPath, eventEmitter);
  const platformInfo = iosProject.getPlatformInfo();

  /**
   * SwiftPM support was introduced in cordova-ios 8.0.0.
   *
   * Hookscript will be be ignored for lower version and fall
   * back to CocoaPods defined in 'plugin.xml'.
   */
  const platformVersion = platformInfo?.version ?? '0.0.0';
  if (semver.lt(platformVersion, '8.0.0')) {
    return;
  }

  // 1. Default version for the Firebase Messaging SDK from plugin.xml
  const pluginPreferences = plugin.pluginInfo.getPreferences('ios');
  // Store the default fallback.
  let firebaseMessagingVersion = pluginPreferences.IOS_FIREBASE_MESSAGING_VERSION;

  // 2. Project's Platform config.xml
  firebaseMessagingVersion = getSDKVersionFromConfigXml(platformInfo?.projectConfig) ||
    firebaseMessagingVersion;

  // 3. Attempt to pull from project's package.json
  firebaseMessagingVersion = await getSDKVersionFromPackageJson(projectRoot, plugin.id) ||
    firebaseMessagingVersion;

  // 4. Use CLI variable, if defined (highest priority)
  firebaseMessagingVersion = getSDKVersionFromCliArguments() ||
    firebaseMessagingVersion;

  // Attempt to update the version in the known Package.swift paths.
  const filesToUpdate = [
    // In: <projects-root>/plugins/@havesource/cordova-plugin-push
    path.join(plugin.dir, 'Package.swift'),
    // In: <projects-root>/platforms/ios/packages/@havesource/cordova-plugin-push
    path.join(platformPath, 'packages', '@havesource', 'cordova-plugin-push', 'Package.swift')
  ];

  for (const file of filesToUpdate) {
    await updateFirebaseMessagingSDKVersion(file, firebaseMessagingVersion);
  }

  console.log(`[cordova-plugin-push] FirebaseMessaging version: ${firebaseMessagingVersion}`);
};

/**
 * Create a key value pair of the defined 'variable' arguments in 'process.argv'
 *
 * @returns {String|null}
 *
 * @throws {Error} when the raw version can not be validated
 */
function getSDKVersionFromCliArguments () {
  const args = nopt(
    // Known options. In this case we only care about variables.
    { variable: Array },
    // Cordova does not support short hand for variables
    {},
    process.argv
  );
  const extractedVariables = args?.variable ?? [];
  const variableEntries = extractedVariables.map(variable => {
    const index = variable.indexOf('='); // Gets the first '='
    if (index === -1) {
      return null;
    }
    return [
      variable.slice(0, index),
      variable.slice(index + 1)
    ];
  }).filter(Boolean);
  const variables = Object.fromEntries(variableEntries);

  const rawVersion = variables?.IOS_FIREBASE_MESSAGING_VERSION;
  if (!rawVersion || rawVersion.trim() === '') {
    return null;
  }

  const version = validateVersion(rawVersion);
  if (version === null) {
    throw new Error(
      `[cordova-plugin-push] Invalid IOS_FIREBASE_MESSAGING_VERSION CLI variable value: ${rawVersion}`
    );
  }

  return version;
}

/**
 * Gets the FirebaseMessaging version from package.json
 *
 * @param {String} projectRoot
 * @param {String} pluginId
 *
 * @returns {String|null}
 *
 * @throws {Error} when the raw version can not be validated
 */
async function getSDKVersionFromPackageJson (projectRoot, pluginId) {
  try {
    const packageJsonRaw = await readFile(path.join(projectRoot, 'package.json'), 'utf8');
    const packageJson = JSON.parse(packageJsonRaw);

    const rawVersion = packageJson?.cordova?.plugins?.[pluginId]?.IOS_FIREBASE_MESSAGING_VERSION;
    if (!rawVersion || rawVersion.trim() === '') {
      return null;
    }

    const version = validateVersion(rawVersion);
    if (version === null) {
      throw new Error(
        `[cordova-plugin-push] Invalid IOS_FIREBASE_MESSAGING_VERSION in package.json: ${rawVersion}`
      );
    }

    return version;
  } catch (e) {
    // If package.json is missing or is not proper JSON, return null to fall back.
    if (e.code === 'ENOENT' || e instanceof SyntaxError) {
      return null;
    }

    // If the file exists but is unable to be accessed (e.g. EACCES),
    // we should throw the error.
    // It is possible that the version was defined, but since there is
    // an issue accessing the file, the defined version cannot be
    // confirmed. Falling back might confuse the app developer.
    throw e;
  }
}

/**
 * Gets the FirebaseMessaging version from config.xml
 *
 * @param {Object} projectConfig
 *
 * @returns {String|null}
 *
 * @throws {Error} when raw version can not be validated
 */
function getSDKVersionFromConfigXml (projectConfig) {
  const rawVersion = projectConfig?.getPreference('IOS_FIREBASE_MESSAGING_VERSION');

  if (!rawVersion || rawVersion.trim() === '') {
    return null;
  }

  const version = validateVersion(rawVersion);
  if (version === null) {
    throw new Error(
      `[cordova-plugin-push] Invalid IOS_FIREBASE_MESSAGING_VERSION in config.xml: ${rawVersion}`
    );
  }

  return version;
}

/**
 * Will attempt to update the SDK version defined in the Package.swift.
 *
 * @param {String} file Package.swift file path
 * @param {String} version FirebaseMessaging version to target
 * @returns {Boolean}
 */
async function updateFirebaseMessagingSDKVersion (file, version) {
  let fh;
  try {
    fh = await open(file, 'r+');
    const content = await fh.readFile('utf8');
    const updatedContent = content.replace(
      // RegEx for matching the line that the version is stored on...
      /let firebaseMessagingSDKVersion:\s*Version\s*=\s*"[^"\r\n]*"/,
      // Replace it with the new version.
      `let firebaseMessagingSDKVersion: Version = "${version}"`
    );
    await fh.truncate(0);
    await fh.write(updatedContent, 0, 'utf8');

    return true;
  } catch (e) {
    if (e.code === 'ENOENT') {
      console.log(`[cordova-plugin-push] File not found, nothing to update. (${file})`);
    } else {
      console.error('[cordova-plugin-push] An error occurred:', e);
    }

    return false;
  } finally {
    if (fh) {
      await fh.close();
    }
  }
}
