# ProGuard rules
-keep public class * { public *; }
-keepclassmembers class * { @com.google.gson.annotations.SerializedName <fields>; }
