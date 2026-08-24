sed -i '/\/\/ Branding/,/      Column {/c\
    // Branding\
    Row(verticalAlignment = Alignment.CenterVertically) {\
      Image(\
        painter = painterResource(id = R.drawable.colorjet_logo),\
        contentDescription = "COLORJET Logo",\
        modifier = Modifier.size(36.dp),\
        contentScale = ContentScale.Fit\
      )\
      Spacer(modifier = Modifier.width(12.dp))\
      Column {' app/src/main/java/com/example/MainActivity.kt
