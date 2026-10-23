# Obfuscate app implementation while preserving Android entry points and public interfaces.
-dontshrink
-dontoptimize
-dontwarn **
-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*
-keep class cn.jingyu.deepseek.NativeActivity { public protected *; }
-keep class cn.jingyu.deepseek.JingyuApp { public protected *; }
-keep class cn.jingyu.deepseek.GeneratedProvider { public protected *; }
# Reflection-based TeX font/parser classes and resource-based loading.
-keep class org.scilab.forge.jlatexmath.** { *; }
-keep class ru.noties.jlatexmath.** { *; }
-keep class io.noties.markwon.** { *; }
-keep class org.commonmark.** { *; }
