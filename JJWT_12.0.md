0.12.4

This patch release:

    Ensures Android environments and older org.json library usages can parse JSON from a JwtBuilder-provided java.io.Reader instance. Issue 882.
    Ensures a single string aud (Audience) claim is retained (without converting it to a Set) when copying/applying a source Claims instance to a destination Claims builder. Issue 890.

0.12.3

This patch release:

    Upgrades the org.json dependency to 20231013 to address that library's CVE-2023-5072 vulnerability.
    (Re-)enables empty values for custom claims, which was the behavior in <= 0.11.5. Issue 858.

0.12.2

This is a follow-up release to finalize the work in 0.12.1 that tried to fix a reflection scope problem on >= JDK 17. The 0.12.1 fix worked, but only if the importing project or application did not have its own module-info.java file.

This release removes that reflection code entirely in favor of a JJWT-native implementation, eliminating JPMS module (scope) problems on >= JDK 17. As such, --add-opens flags are no longer required to use JJWT.

The fix has been tested up through JDK 21 in a separate application environment (out of JJWT's codebase) to assert expected functionality in a 'clean room' environment in a project both with and without module-info.java usage.
0.12.1

Enabled reflective access on JDK 17+ to java.io.ByteArrayInputStream and sun.security.util.KeyUtil for jjwt-impl.jar
0.12.0

This is a big release! JJWT now fully supports Encrypted JSON Web Tokens (JWE), JSON Web Keys (JWK) and more! See the sections below enumerating all new features as well as important notes on breaking changes or backwards-incompatible changes made in preparation for the upcoming 1.0 release.

Because breaking changes are being introduced, it is strongly recommended to wait until the upcoming 1.0 release where you can address breaking changes one time only.

Those that need immediate JWE encryption and JWK key support however will likely want to upgrade now and deal with the smaller subset of breaking changes in the 1.0 release.
Simplified Starter Jar

Those upgrading to new modular JJWT versions from old single-jar versions will transparently obtain everything they need in their Maven, Gradle or Android projects.

JJWT's early releases had one and only one .jar: jjwt.jar. Later releases moved to a modular design with 'api' and 'impl' jars including 'plugin' jars for Jackson, GSON, org.json, etc. Some users upgrading from the earlier single jar to JJWT's later versions have been frustrated by being forced to learn how to configure the more modular .jars.

This release re-introduces the jjwt.jar artifact again, but this time it is simply an empty .jar with Maven metadata that will automatically transitively download the following into a project, retaining the old single-jar behavior:

    jjwt-api.jar
    jjwt-impl.jar
    jjwt-jackson.jar

Naturally, developers are still encouraged to configure the modular .jars as described in JJWT's documentation for greater control and to enable their preferred JSON parser, but this stop-gap should help those unaware when upgrading.
JSON Web Encryption (JWE) Support!

This has been a long-awaited feature for JJWT, years in the making, and it is quite extensive - so many encryption algorithms and key management algorithms are defined by the JWA specification, and new API concepts had to be introduced for all of them, as well as extensive testing with RFC-defined test vectors. The wait is over!
All JWA-defined encryption algorithms and key management algorithms are fully implemented and supported and available immediately. For example:

AeadAlgorithm enc = Jwts.ENC.A256GCM;
SecretKey key = enc.key().build();
String compact = Jwts.builder().setSubject("Joe").encryptWith(key, enc).compact();

Jwe<Claims> jwe = Jwts.parser().decryptWith(key).build().parseEncryptedClaims(compact);

Many other RSA and Elliptic Curve examples are in the full README documentation.
JSON Web Key (JWK) Support!

Representing cryptographic keys - SecretKeys, RSA Public and Private Keys, Elliptic Curve Public and Private keys - as fully encoded JSON objects according to the JWK specification - is now fully implemented and supported. The new Jwks utility class exists to create JWK builders and parsers as desired. For example:

SecretKey key = Jwts.SIG.HS256.key().build();
SecretJwk jwk = Jwks.builder().forKey(key).build();
assert key.equals(jwk.toKey());

// or if receiving a JWK string:
Jwk<?> parsedJwk = Jwks.parser().build().parse(jwkString);
assert jwk.equals(parsedJwk);
assert key.equals(parsedJwk.toKey());

Many JJWT users won't need to use JWKs explicitly, but some JWA Key Management Algorithms (and lots of RFC test vectors) utilize JWKs when transmitting JWEs. As this was required by JWE, it is now implemented in full for JWE use as well as general-purpose JWK support.
JWK Thumbprint and JWK Thumbprint URI support

The JWK Thumbprint and JWK Thumbprint URI RFC specifications are now fully supported. Please see the README.md file's corresponding named sections for both for full documentation and usage examples.
JWS Unencoded Payload Option (b64) support

The JSON Web Signature (JWS) Unencoded Payload Option RFC specification is now fully supported. Please see the README.md corresponding named section for documentation and usage examples.
Better PKCS11 and Hardware Security Module (HSM) support

Previous versions of JJWT enforced that Private Keys implemented the RSAKey and ECKey interfaces to enforce key length requirements. With this release, JJWT will still perform those checks when those data types are available, but if not, as is common with keys from PKCS11 and HSM KeyStores, JJWT will still allow those Keys to be used, expecting the underlying Security Provider to enforce any key requirements. This should reduce or eliminate any custom code previously written to extend JJWT to use keys from those KeyStores or Providers.

Additionally, PKCS11/HSM tests using SoftHSMv2 are run on every build with every JWS MAC and Signature algorithm and every JWE Key algorithm to ensure continued stable support with Android and Sun PKCS11 implementations and spec-compliant Hardware Security Modules that use the PKCS11 interface (such as YubiKey, etc.)
Custom Signature Algorithms

The io.jsonwebtoken.SignatureAlgorithm enum has been deprecated in favor of new io.jsonwebtoken.security.SecureDigestAlgorithm, io.jsonwebtoken.security.MacAlgorithm, and io.jsonwebtoken.security.SignatureAlgorithm interfaces to allow custom algorithm implementations. The new nested Jwts.SIG static inner class is a registry of all standard JWS algorithms as expected, exactly like the old enum. This change was made because enums are a static concept by design and cannot support custom values: those who wanted to use custom signature algorithms could not do so until now. The new interfaces now allow anyone to plug in and support custom algorithms with JJWT as desired.
KeyBuilder and KeyPairBuilder

Because the io.jsonwebtoken.security.Keys#secretKeyFor and io.jsonwebtoken.security.Keys#keyPairFor methods accepted the now-deprecated io.jsonwebtoken.SignatureAlgorithm enum, they have also been deprecated in favor of calling new key() or keyPair() builder methods on MacAlgorithm and SignatureAlgorithm instances directly.
For example:

SecretKey key = Jwts.SIG.HS256.key().build();
KeyPair pair = Jwts.SIG.RS256.keyPair().build();

The builders allow for customization of the JCA Provider and SecureRandom during Key or KeyPair generation if desired, whereas the old enum-based static utility methods did not.
Preparation for 1.0

Now that the JWE and JWK specifications are implemented, only a few things remain for JJWT to be considered at version 1.0. We have been waiting to apply the 1.0 release version number until the entire set of JWT specifications are fully supported and we drop JDK 7 support (to allow users to use JDK 8 APIs). To that end, we have had to deprecate some concepts, or in some cases, completely break backwards compatibility to ensure the transition to 1.0 (and JDK 8 APIs) are possible. Most backwards-incompatible changes are listed in the next section below.
Backwards Compatibility Breaking Changes, Warnings and Deprecations

    io.jsonwebtoken.Jwt's getBody() method has been deprecated in favor of a new getPayload() method to reflect correct JWT specification nomenclature/taxonomy.

    io.jsonwebtoken.Jws's getSignature() method has been deprecated in favor of a new getDigest() method to support expected congruent behavior with Jwe instances (both have digests).

    io.jsonwebtoken.JwtParser's parseContentJwt, parseClaimsJwt, parseContentJws, and parseClaimsJws methods have been deprecated in favor of more intuitive respective parseUnsecuredContent, parseUnsecuredClaims, parseSignedContent and parseSignedClaims methods.

    io.jsonwebtoken.CompressionCodec is now deprecated in favor of the new io.jsonwebtoken.io.CompressionAlgorithm interface. This is to guarantee API congruence with all other JWT-identifiable algorithm IDs that can be set as a header value.

    io.jsonwebtoken.CompressionCodecResolver has been deprecated in favor of the new JwtParserBuilder#addCompressionAlgorithms method.

Breaking Changes

    io.jsonwebtoken.Claims and io.jsonwebtoken.Header instances are now immutable to enhance security and thread safety. Creation and mutation are supported with newly introduced ClaimsBuilder and HeaderBuilder concepts. Even though mutation methods have migrated, there are a couple that have been removed entirely:
        io.jsonwebtoken.JwsHeader#setAlgorithm has been removed - the JwtBuilder will always set the appropriate alg header automatically based on builder state.
        io.jsonwebtoken.Header#setCompressionAlgorithm has been removed - the JwtBuilder will always set the appropriate zip header automatically based on builder state.

    io.jsonwebtoken.Jwts's header(Map), jwsHeader() and jwsHeader(Map) methods have been removed in favor of the new header() method that returns a HeaderBuilder to support method chaining and dynamic Header type creation. The HeaderBuilder will dynamically create a Header, JwsHeader or JweHeader automatically based on builder state.

    Similarly, io.jsonwebtoken.Jwts's claims() static method has been changed to return a ClaimsBuilder instead of a Claims instance.

    JWTs that do not contain JSON Claims now have a payload type of byte[] instead of String (that is, Jwt<byte[]> instead of Jwt<String>). This is because JWTs, especially when used with the cty (Content Type) header, are capable of handling any type of payload, not just Strings. The previous JJWT releases didn't account for this, and now the API accurately reflects the JWT RFC specification payload capabilities. Additionally, the name of plaintext has been changed to content in method names and JavaDoc to reflect this taxonomy. This change has impacted the following JJWT APIs:

        The JwtBuilder's setPayload(String) method has been deprecated in favor of two new methods:
            setContent(byte[]), and
            setContent(byte[], String contentType)

        These new methods allow any kind of content within a JWT, not just Strings. The existing setPayload(String) method implementation has been changed to delegate to this new setContent(byte[]) method with the argument's UTF-8 bytes, for example setContent(payloadString.getBytes(StandardCharsets.UTF_8)).

        The JwtParser's Jwt<Header, String> parsePlaintextJwt(String plaintextJwt) and Jws<String> parsePlaintextJws(String plaintextJws) methods have been changed to Jwt<Header, byte[]> parseContentJwt(String plaintextJwt) and Jws<byte[]> parseContentJws(String plaintextJws) respectively.

        JwtHandler's onPlaintextJwt(String) and onPlaintextJws(String) methods have been changed to onContentJwt(byte[]) and onContentJws(byte[]) respectively.

        io.jsonwebtoken.JwtHandlerAdapter has been changed to reflect the above-mentioned name and String-to-byte[] argument changes, as well adding the abstract modifier. This class was never intended to be instantiated directly, and is provided for subclassing only. The missing modifier has been added to ensure the class is used as it had always been intended.

        io.jsonwebtoken.SigningKeyResolver's resolveSigningKey(JwsHeader, String) method has been changed to resolveSigningKey(JwsHeader, byte[]).

    io.jsonwebtoken.JwtParser is now immutable. All mutation/modification methods (setters, etc) deprecated 4 years ago have been removed. All parser configuration requires using the JwtParserBuilder.

    Similarly, io.jsonwebtoken.Jwts's parser() method deprecated 4 years ago has been changed to now return a JwtParserBuilder instead of a direct JwtParser instance. The previous Jwts.parserBuilder() method has been removed as it is now redundant.

    The JwtParserBuilder no longer supports PrivateKeys for signature verification. This was an old legacy behavior scheduled for removal years ago, and that change is now complete. For various cryptographic/security reasons, asymmetric public/private key signatures should always be created with PrivateKeys and verified with PublicKeys.

    io.jsonwebtoken.CompressionCodec implementations are no longer discoverable via java.util.ServiceLoader due to runtime performance problems with the JDK's ServiceLoader implementation per #648. Custom implementations should be made available to the JwtParser via the new JwtParserBuilder#addCompressionAlgorithms method.

    Prior to this release, if there was a serialization problem when serializing the JWT Header, an IllegalStateException was thrown. If there was a problem when serializing the JWT claims, an IllegalArgumentException was thrown. This has been changed up to ensure consistency: any serialization error with either headers or claims will now throw a io.jsonwebtoken.io.SerializationException.

    Parsing of unsecured JWTs (alg header of none) are now disabled by default as mandated by RFC 7518, Section 3.6. If you require parsing of unsecured JWTs, you must call the JwtParserBuilder#enableUnsecured() method, but note the security implications mentioned in that method's JavaDoc before doing so.

    io.jsonwebtoken.gson.io.GsonSerializer now requires Gson instances that have a registered GsonSupplierSerializer type adapter, for example:

    new GsonBuilder()
      .registerTypeHierarchyAdapter(io.jsonwebtoken.lang.Supplier.class, GsonSupplierSerializer.INSTANCE)    
      .disableHtmlEscaping().create();

This is to ensure JWKs have toString() and application log safety (do not print secure material), but still serialize to JSON correctly.

io.jsonwebtoken.InvalidClaimException and its two subclasses (IncorrectClaimException and MissingClaimException) were previously mutable, allowing the corresponding claim name and claim value to be set on the exception after creation. These should have always been immutable without those setters (just getters), and this was a previous implementation oversight. This release has ensured they are immutable without the setters.
