sed -i '/package com.example/d' app/src/main/java/com/example/MainActivity.kt
sed -i '1i package com.example' app/src/main/java/com/example/MainActivity.kt

sed -i '/package com.example.ui/d' app/src/main/java/com/example/ui/LoginScreen.kt
sed -i '1i package com.example.ui' app/src/main/java/com/example/ui/LoginScreen.kt

sed -i '/package com.example.ui/d' app/src/main/java/com/example/ui/CustomerDashboard.kt
sed -i '1i package com.example.ui' app/src/main/java/com/example/ui/CustomerDashboard.kt
