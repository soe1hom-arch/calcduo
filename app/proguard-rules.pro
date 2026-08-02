# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep ViewBinding classes
-keep class com.soe1hom.calcduo.databinding.** { *; }

# Keep data classes used in state management
-keep class com.soe1hom.calcduo.data.** { *; }

# Keep enum/sealed class values
-keepclassmembers enum * { *; }
