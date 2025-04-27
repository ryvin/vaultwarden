//! Thin JNI layer to start/stop vaultwarden inside Android app

use jni::objects::{JClass, JObject, JString};
use jni::sys::{jint, jlong};
use jni::JNIEnv;
use rcgen::{Certificate, CertificateParams, DistinguishedName, SanType};
use std::fs;
use std::path::Path;
use std::sync::Once;
use std::thread;

static START: Once = Once::new();
static mut SERVER_HANDLE: Option<thread::JoinHandle<()>> = None;

#[no_mangle]
pub extern "system" fn Java_com_vw_ServerBridge_startServer(env: JNIEnv, _class: JClass, data_dir: JString, port: jint) {
    START.call_once(|| {
        // Retrieve data directory path from Java
        let data_dir_str = env.get_string(&data_dir).expect("Couldn't get Java string!").into();
        let data_path = Path::new(&data_dir_str);

        // Define cert/key paths
        let cert_path = data_path.join("cert.pem");
        let key_path = data_path.join("key.pem");

        // Generate cert/key if they don't exist
        if !cert_path.exists() || !key_path.exists() {
            println!("Generating self-signed certificate for 127.0.0.1...");
            let mut params = CertificateParams::new(vec!["127.0.0.1".to_string()]);
            params.distinguished_name = DistinguishedName::new(); // Use defaults
            params.subject_alt_names.push(SanType::IpAddress(std::net::Ipv4Addr::new(127, 0, 0, 1).into()));

            let cert = Certificate::from_params(params).expect("Certificate generation failed");
            let cert_pem = cert.serialize_pem().expect("Certificate serialization failed");
            let key_pem = cert.serialize_private_key_pem();

            fs::write(&cert_path, cert_pem).expect("Failed to write cert file");
            fs::write(&key_path, key_pem).expect("Failed to write key file");
            println!("Certificate and key saved to {:?} and {:?}", cert_path, key_path);
        }

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
