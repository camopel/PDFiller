package edu.sjsu.yduan.PDFiller;

import android.util.Log;
import com.tom_roush.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.spongycastle.asn1.ASN1Primitive;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaCertStore;
import org.spongycastle.cms.CMSSignedData;
import org.spongycastle.cms.CMSSignedDataGenerator;
import org.spongycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;
import org.spongycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.spongycastle.util.Store;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Signer implements SignatureInterface {
    private PrivateKey privateKey;
    private Certificate certificate;
    public Signer(KeyStore keystore, char[] pin){
       try {
           Enumeration<String> aliases = keystore.aliases();
           String alias;
           Certificate cert = null;
           while(aliases.hasMoreElements())
           {
               alias = aliases.nextElement();
               setPrivateKey((PrivateKey) keystore.getKey(alias, pin));
               Certificate[] certChain = keystore.getCertificateChain(alias);
               if (certChain == null) continue;
               cert = certChain[0];
               setCertificate(cert);
               if (cert instanceof X509Certificate) ((X509Certificate) cert).checkValidity();
               break;
           }
       }catch (Exception ex){
           Log.d("Exception",ex.getMessage());
       }
    }
    public final void setPrivateKey(PrivateKey privateKey)
    {
        this.privateKey = privateKey;
    }
    public final void setCertificate(Certificate certificate)
    {
        this.certificate = certificate;
    }
    @Override
    public byte[] sign(InputStream content)
    {
        try
        {
            List<Certificate> certList = new ArrayList<Certificate>();
            certList.add(certificate);
            Store certs = new JcaCertStore(certList);
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            org.spongycastle.asn1.x509.Certificate cert = org.spongycastle.asn1.x509.Certificate.getInstance(ASN1Primitive.fromByteArray(certificate.getEncoded()));
            ContentSigner sha1Signer = new JcaContentSignerBuilder("SHA256WithRSA").build(privateKey);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(sha1Signer, new X509CertificateHolder(cert)));
            gen.addCertificates(certs);
            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);
            return signedData.getEncoded();
        }
        catch (Exception ex){
            Log.d("Exception",ex.getMessage());
        }
        return null;
    }
}
