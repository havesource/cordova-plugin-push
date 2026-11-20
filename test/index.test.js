const { describe, it, beforeEach, afterEach, mock } = require('node:test');
const assert = require('node:assert/strict');
const { setTimeout: delay } = require('node:timers/promises');

const cordova = require('./helper/cordova');
const PushNotification = require('../www/push');

let execSpy;
let execWin;
let options;

// MARK: Helpers

// Wrapping push events with a promise to resolve when emitted
const nextEventPromise = (push, name) => new Promise((resolve) => push.on(name, resolve));

const assertExecCalledWith = (action, expectedArgs) => {
  const call = execSpy.mock.calls.find(c => c.arguments[3] === action);

  assert.ok(call, `expected cordova/exec to be called with "${action}"`);

  const [win, fail, service, , args] = call.arguments;

  assert.strictEqual(typeof win, 'function');
  assert.strictEqual(typeof fail, 'function');
  assert.strictEqual(service, 'PushNotification');

  if (expectedArgs) {
    assert.deepStrictEqual(args, expectedArgs);
  } else {
    assert.ok(Array.isArray(args));
  }
};

// MARK: Tests

describe('cordova-plugin-push', () => {
  beforeEach(() => {
    options = { android: {}, ios: {} };
    execWin = mock.fn();
    execSpy = mock.method(cordova.required, 'cordova/exec', execWin);
  });

  afterEach(() => {
    mock.restoreAll();
  });

  describe('PushNotification', () => {
    it('should exist', () => {
      assert.notStrictEqual(PushNotification, undefined);
      assert.strictEqual(typeof PushNotification, 'object');
    });

    it('should contain a init function', () => {
      assert.notStrictEqual(PushNotification.init, undefined);
      assert.strictEqual(typeof PushNotification.init, 'function');
    });

    it('should contain a hasPermission function', () => {
      assert.notStrictEqual(PushNotification.hasPermission, undefined);
      assert.strictEqual(typeof PushNotification.hasPermission, 'function');
    });

    it('should contain a createChannel function', () => {
      assert.notStrictEqual(PushNotification.createChannel, undefined);
      assert.strictEqual(typeof PushNotification.createChannel, 'function');
    });

    it('should contain a deleteChannel function', () => {
      assert.notStrictEqual(PushNotification.deleteChannel, undefined);
      assert.strictEqual(typeof PushNotification.deleteChannel, 'function');
    });

    it('should contain a listChannels function', () => {
      assert.notStrictEqual(PushNotification.listChannels, undefined);
      assert.strictEqual(typeof PushNotification.listChannels, 'function');
    });

    it('should contain a unregister function', () => {
      const push = PushNotification.init({});
      assert.notStrictEqual(push.unregister, undefined);
      assert.strictEqual(typeof push.unregister, 'function');
    });

    it('should contain a getApplicationIconBadgeNumber function', () => {
      const push = PushNotification.init({});
      assert.notStrictEqual(push.getApplicationIconBadgeNumber, undefined);
      assert.strictEqual(typeof push.getApplicationIconBadgeNumber, 'function');
    });

    it('should contain a setApplicationIconBadgeNumber function', () => {
      const push = PushNotification.init({});
      assert.notStrictEqual(push.setApplicationIconBadgeNumber, undefined);
      assert.strictEqual(typeof push.setApplicationIconBadgeNumber, 'function');
    });

    it('should contain a clearAllNotifications function', () => {
      const push = PushNotification.init({});
      assert.notStrictEqual(push.clearAllNotifications, undefined);
      assert.strictEqual(typeof push.clearAllNotifications, 'function');
    });

    it('should contain a clearNotification function', () => {
      const push = PushNotification.init({});
      assert.notStrictEqual(push.clearNotification, undefined);
      assert.strictEqual(typeof push.clearNotification, 'function');
    });

    it('should contain a subscribe function', () => {
      const push = PushNotification.init({});
      assert.notStrictEqual(push.subscribe, undefined);
      assert.strictEqual(typeof push.subscribe, 'function');
    });

    it('should contain a unsubscribe function', () => {
      const push = PushNotification.init({});
      assert.notStrictEqual(push.unsubscribe, undefined);
      assert.strictEqual(typeof push.unsubscribe, 'function');
    });
  });

  describe('PushNotification instance', () => {
    describe('cordova.exec', () => {
      it('should call cordova.exec on next process tick', async () => {
        PushNotification.init(options);
        await delay(100);
        assertExecCalledWith('init');
      });
    });

    describe('on "registration" event', () => {
      it('should be emitted with an argument', async () => {
        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win({ registrationId: 1 });
        });
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'registration');
        assert.strictEqual(data.registrationId, 1);
      });
    });

    describe('on "notification" event', () => {
      beforeEach(() => {
        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win({
            message: 'Message',
            title: 'Title',
            count: 1,
            sound: 'beep',
            image: 'Image',
            additionalData: {}
          });
        });
      });

      it('should be emitted on success', async () => {
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'notification');
        assert.ok(data);
      });

      it('should provide the data.message argument', async () => {
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'notification');
        assert.strictEqual(data.message, 'Message');
      });

      it('should provide the data.title argument', async () => {
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'notification');
        assert.strictEqual(data.title, 'Title');
      });

      it('should provide the data.count argument', async () => {
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'notification');
        assert.strictEqual(data.count, 1);
      });

      it('should provide the data.sound argument', async () => {
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'notification');
        assert.strictEqual(data.sound, 'beep');
      });

      it('should provide the data.image argument', async () => {
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'notification');
        assert.strictEqual(data.image, 'Image');
      });

      it('should provide the data.additionalData argument', async () => {
        const push = PushNotification.init(options);
        const data = await nextEventPromise(push, 'notification');
        assert.deepStrictEqual(data.additionalData, {});
      });
    });

    describe('on "error" event', () => {
      it('should be emitted with an Error', async () => {
        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          fail('something went wrong');
        });
        const push = PushNotification.init(options);
        const e = await nextEventPromise(push, 'error');
        assert.ok(e instanceof Error);
        assert.strictEqual(e.message, 'something went wrong');
      });
    });

    describe('off "notification" event', () => {
      it('should exist and be registered a callback handle', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        push.on('notification', eventHandler);
        push.off('notification', eventHandler);

        assert.strictEqual(push.handlers.notification.indexOf(eventHandler), -1);
      });
    });

    describe('off "registration" event', () => {
      it('should exist and be registered a callback handle', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        push.on('registration', eventHandler);
        push.off('registration', eventHandler);

        assert.strictEqual(push.handlers.registration.indexOf(eventHandler), -1);
      });
    });

    describe('off "error" event', () => {
      it('should exist and be registered a callback handle', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        push.on('error', eventHandler);
        push.off('error', eventHandler);

        assert.strictEqual(push.handlers.error.indexOf(eventHandler), -1);
      });
    });

    describe('unregister method', () => {
      it('should clear "registration" event handlers', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        assert.strictEqual(push.handlers.registration.length, 0);

        push.on('registration', eventHandler);

        assert.strictEqual(push.handlers.registration.length, 1);
        assert.ok(push.handlers.registration.indexOf(eventHandler) > -1);

        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win();
        });

        const onSuccess = mock.fn(() => {
          assert.strictEqual(push.handlers.registration.length, 0);
          assert.strictEqual(push.handlers.registration.indexOf(eventHandler), -1);
        });

        push.unregister(onSuccess);

        assert.strictEqual(onSuccess.mock.callCount(), 1);
      });

      it('should clear "notification" event handlers', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        assert.strictEqual(push.handlers.notification.length, 0);

        push.on('notification', eventHandler);

        assert.strictEqual(push.handlers.notification.length, 1);
        assert.ok(push.handlers.notification.indexOf(eventHandler) > -1);

        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win();
        });

        const onSuccess = mock.fn(() => {
          assert.strictEqual(push.handlers.notification.length, 0);
          assert.strictEqual(push.handlers.notification.indexOf(eventHandler), -1);
        });

        push.unregister(onSuccess);

        assert.strictEqual(onSuccess.mock.callCount(), 1);
      });

      it('should clear "error" event handlers', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        assert.strictEqual(push.handlers.error.length, 0);

        push.on('error', eventHandler);

        assert.strictEqual(push.handlers.error.length, 1);
        assert.ok(push.handlers.error.indexOf(eventHandler) > -1);

        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win();
        });

        const onSuccess = mock.fn(() => {
          assert.strictEqual(push.handlers.error.length, 0);
          assert.strictEqual(push.handlers.error.indexOf(eventHandler), -1);
        });

        push.unregister(onSuccess);

        assert.strictEqual(onSuccess.mock.callCount(), 1);
      });
    });

    describe('unregister topics method', () => {
      it('should not clear "registration" event handlers', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        assert.strictEqual(push.handlers.registration.length, 0);

        push.on('registration', eventHandler);

        assert.strictEqual(push.handlers.registration.length, 1);
        assert.ok(push.handlers.registration.indexOf(eventHandler) > -1);

        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win();
        });

        const onSuccess = mock.fn(() => {
          assert.strictEqual(push.handlers.registration.length, 1);
          assert.ok(push.handlers.registration.indexOf(eventHandler) > -1);
        });

        push.unregister(onSuccess, () => {}, ['foo', 'bar']);

        assert.strictEqual(onSuccess.mock.callCount(), 1);
      });

      it('should not clear "notification" event handlers', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        assert.strictEqual(push.handlers.notification.length, 0);

        push.on('notification', eventHandler);

        assert.strictEqual(push.handlers.notification.length, 1);
        assert.ok(push.handlers.notification.indexOf(eventHandler) > -1);

        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win();
        });

        const onSuccess = mock.fn(() => {
          assert.strictEqual(push.handlers.notification.length, 1);
          assert.ok(push.handlers.notification.indexOf(eventHandler) > -1);
        });

        push.unregister(onSuccess, () => {}, ['foo', 'bar']);

        assert.strictEqual(onSuccess.mock.callCount(), 1);
      });

      it('should not clear "error" event handlers', () => {
        const push = PushNotification.init(options);
        const eventHandler = () => {};

        assert.strictEqual(push.handlers.error.length, 0);

        push.on('error', eventHandler);

        assert.strictEqual(push.handlers.error.length, 1);
        assert.ok(push.handlers.error.indexOf(eventHandler) > -1);

        execSpy.mock.mockImplementation((win, fail, service, id, args) => {
          win();
        });

        const onSuccess = mock.fn(() => {
          assert.strictEqual(push.handlers.error.length, 1);
          assert.ok(push.handlers.error.indexOf(eventHandler) > -1);
        });

        push.unregister(onSuccess, () => {}, ['foo', 'bar']);

        assert.strictEqual(onSuccess.mock.callCount(), 1);
      });
    });

    describe('subscribe topic method', () => {
      describe('cordova.exec', () => {
        it('should call cordova.exec on next process tick', async () => {
          const push = PushNotification.init(options);
          push.subscribe('foo', () => {}, () => {});
          await delay(100);
          assertExecCalledWith('subscribe');
        });
      });
    });

    describe('unsubscribe topic method', () => {
      describe('cordova.exec', () => {
        it('should call cordova.exec on next process tick', async () => {
          const push = PushNotification.init(options);
          push.unsubscribe('foo', () => {}, () => {});
          await delay(100);
          assertExecCalledWith('unsubscribe');
        });
      });
    });

    describe('clear notification method', () => {
      describe('cordova.exec', () => {
        it('should call cordova.exec on next process tick using number argument', async () => {
          const push = PushNotification.init(options);
          push.clearNotification(() => {}, () => {}, 145);
          await delay(100);
          assertExecCalledWith('clearNotification', [145]);
        });

        it('should call cordova.exec on next process tick using string argument', async () => {
          const push = PushNotification.init(options);
          push.clearNotification(() => {}, () => {}, '145');
          await delay(100);
          assertExecCalledWith('clearNotification', [145]);
        });
      });
    });
  });
});
