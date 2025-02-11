package com.shyampatel.core.common.jwt
import io.jsonwebtoken.Jwts
import java.security.KeyFactory
import java.security.spec.PKCS8EncodedKeySpec
import java.util.Date
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@OptIn(ExperimentalEncodingApi::class)
fun generateJwtTokenRsa(subject: String? = null,
                        expiry: Date? = null,
                        issuedAt: Date? = null,
                        issuer: String? = null,
                        claims: Map<String, Any>? = null,
                        key: String): String {

    val privateKeyPEM: String = key
        .replace("-----BEGIN PRIVATE KEY-----", "")
        .replace("-----END PRIVATE KEY-----", "")
        .replace(System.lineSeparator(), "")
    val decoded = Base64.decode(privateKeyPEM)

    val keySpec = PKCS8EncodedKeySpec(decoded)
    val kf = KeyFactory.getInstance("RSA")
    val prvKey = kf.generatePrivate(keySpec)

    return Jwts.builder()
        .subject(subject)
        .claims(claims)
        .issuer(issuer)
        .issuedAt(issuedAt)
        .expiration(expiry)
        .signWith(prvKey, Jwts.SIG.RS256)
        .compact()
}