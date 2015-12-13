-injars       target/bunkr_beta-0.1.jar
-outjars      target/bunkr_beta-0.1-release.jar
-libraryjars  <java.home>/lib/rt.jar

-keep public class com.bunkr_beta.cli.CLI {
    public static void main(java.lang.String[]);
}

-dontwarn javax.crypto.**
-dontwarn org.bouncycastle.jcajce.provider.asymmetric.util.BaseCipherSpi
-keepattributes *Annotation*
-keepattributes Signature

-dontoptimize
-dontobfuscate