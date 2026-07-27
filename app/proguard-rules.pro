# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable

# Keep ViewBinding classes
-keep class com.calculator.app.databinding.** { *; }

# Keep data classes used in state management
-keep class com.calculator.app.data.** { *; }

# Keep enum/sealed class values
-keepclassmembers enum * { *; }
