-injars       target/bunkr-0.2.jar
-outjars      target/bunkr-0.2-release.jar
-libraryjars  <java.home>/lib/rt.jar

-keep public class org.bunkr.cli.CLI {
    public static void main(java.lang.String[]);
}

-dontwarn javax.crypto.**
-dontwarn org.bouncycastle.jcajce.provider.asymmetric.util.BaseCipherSpi
-keepattributes *Annotation*
-keepattributes Signature

-dontoptimize
-dontobfuscate