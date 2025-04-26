//! Thin JNI layer to start/stop vaultwarden inside Android app

use jni::objects::{JClass, JObject, JString};
use jni::sys::{jint, jlong};
use jni::JNIEnv;
use std::sync::Once;
use std::thread;

static START: Once = Once::new();
static mut SERVER_HANDLE: Option<thread::JoinHandle<()>> = None;

#[no_mangle]
pub extern "system" fn Java_com_vw_ServerBridge_startServer(env: JNIEnv, _class: JClass, port: jint) {
    START.call_once(|| {
        let port = port as u16;
        unsafe {
            SERVER_HANDLE = Some(thread::spawn(move || {
                std::env::set_var("ROCKET_PORT", port.to_string());
                let rt = tokio::runtime::Runtime::new().unwrap();
                rt.block_on(async {
                    vaultwarden::start_server().await;
                });
            }));
        }
    });
}

#[no_mangle]
pub extern "system" fn Java_com_vw_ServerBridge_stopServer(_env: JNIEnv, _class: JClass) {
    unsafe {
        if let Some(handle) = SERVER_HANDLE.take() {
            // Placeholder: implement graceful shutdown using channel/signal
            let _ = handle.join();
        }
    }
}
