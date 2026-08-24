sed -i '/                Box(/,/                    Icon(Icons.Default.Cloud, contentDescription = "Logo", tint = Color.White)\n                }/c\
                Image(\
                    painter = painterResource(id = R.drawable.colorjet_logo),\
                    contentDescription = "COLORJET Logo",\
                    modifier = Modifier.size(36.dp),\
                    contentScale = ContentScale.Fit\
                )' app/src/main/java/com/example/ui/CustomerDashboard.kt
