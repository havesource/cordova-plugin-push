const { join } = require('path');
const { existsSync, readFileSync, writeFileSync } = require('fs');
const { parseElementtreeSync: ParseElementtreeSync } = require('cordova-common/src/util/xml-helpers');
const platform = require('cordova-android');

module.exports = function (context) {
  if (!isExecutable()) {
    console.log('[cordova-plugin-push::before-compile] skipping before_compile hookscript.');
    return;
  }

  const buildGradleFilePath = join(context.opts.projectRoot, 'platforms/android/build.gradle');

  if (!existsSync(buildGradleFilePath)) {
    console.log('[cordova-plugin-push::before-compile] could not find "build.gradle" file.');
    return;
  }

  updateBuildGradle(context, buildGradleFilePath);
};

/**
 * This hookscript is executable only when the platform version less then 10.x
 * @returns Boolean
 */
function isExecutable () {
  const majorVersion = parseInt(platform.version(), 10);
  return majorVersion < 10 && majorVersion >= 9;
}

function getPluginKotlinVersion (context) {
  const pluginConfig = new ParseElementtreeSync(join(context.opts.projectRoot, 'plugins/@havesource/cordova-plugin-push/plugin.xml'));

  return pluginConfig
    .findall('./platform[@name="android"]').pop()
    .findall('./config-file[@target="config.xml"]').pop()
    .findall('preference').filter(
      elem => elem.attrib.name.toLowerCase() === 'GradlePluginKotlinVersion'.toLowerCase()
    ).pop().attrib.value;
}

function updateBuildGradle (context, buildGradleFilePath) {
  const kotlinVersion = getPluginKotlinVersion(context);
  const updateContent = readFileSync(buildGradleFilePath, 'utf8')
    .replace(/ext.kotlin_version = ['"](.*)['"]/g, `ext.kotlin_version = '${kotlinVersion}'`);

  writeFileSync(buildGradleFilePath, updateContent);

  console.log(`[cordova-plugin-push::before-compile] updated "build.gradle" file with kotlin version set to: ${kotlinVersion}`);
}
