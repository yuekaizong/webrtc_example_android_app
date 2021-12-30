package aga.songmaya.support.utils;

import android.util.Log;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class HttpUtils {

    private static final String TAG = HttpUtils.class.getSimpleName();

    /**
     * 用HttpUrlConnection做https请求支持所有证书
     * HttpURLConnection调用openConnection之前，执行一下trustAllHosts方法，信任所有证书，
     * 可解决CertPathValidatorException: Trust anchor for certification path not found.
     * 尝试解决SSLHandshakeException: Handshake failed 无效
     */
    public static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkClientTrusted");
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                Log.i(TAG, "checkServerTrusted");
            }
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 尝试解决SSLHandshakeException: Handshake failed 无效
     */
    public static void setHttps() {
        System.setProperty("https.protocols", "TLSv1,SSLv3");
    }

    /**
     *解决Hostname $ip not verified
     */
    public static void setDefaultHostnameVerify() {
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            public boolean verify(String string, SSLSession ssls) {
                return true;
            }
        });
    }

    /**
     * 后台nohup.collider.out出现错误：
     * http: TLS handshake error from 117.30.197.225:42130: remote error: tls: unknown certificate
     */

}
