sed -i '/\/\/ Enterprise Logo & Branding/,/Column(horizontalAlignment = Alignment.CenterHorizontally) {/c\
            // Enterprise Logo & Branding\
            Image(\
                painter = painterResource(id = R.drawable.colorjet_logo_text),\
                contentDescription = "COLORJET Logo",\
                modifier = Modifier.width(200.dp).height(60.dp),\
                contentScale = ContentScale.Fit\
            )\
            Column(horizontalAlignment = Alignment.CenterHorizontally) {' app/src/main/java/com/example/ui/LoginScreen.kt
