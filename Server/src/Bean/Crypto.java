package Bean;

import util.Base64Utils;
import util.DBUtil;
import util.RSAUtils;

import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.sql.Connection;

import service.UserService;

public class Crypto {
	
    private String pk_exp;
    private String sk_exp;
    private String modulus;
    public final static int ADDR_LENGTH = 20;
    private KeyPair keypair;

	public String generateQRcontent(double value, int id) {
		
		//generate key
		keypair = RSAUtils.generateRSAKeyPair(512);
        RSAPublicKey publicKey = (RSAPublicKey) keypair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keypair.getPrivate();

        pk_exp = Base64Utils.encode(publicKey.getPublicExponent().toByteArray());
        sk_exp = Base64Utils.encode(privateKey.getPrivateExponent().toByteArray());
        modulus = Base64Utils.encode(publicKey.getModulus().toByteArray());
	    
        UserService userService = new UserService();
        Connection conn = DBUtil.getConn();
        String result = userService.cryptomoney(conn, id, value, modulus,pk_exp,1);
	    
		return result;
	}
	
	public String getKey() {
		return sk_exp;
	}
	
	public String getMod() {
		return modulus;
	}
}
