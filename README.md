# AndroidCarrouselLayout
a carrousel layout for android

#Preview
![image](https://github.com/dalong982242260/AndroidCarrouselLayout/blob/master/gif/carrousel01.gif?raw=true)
![image](https://github.com/dalong982242260/AndroidCarrouselLayout/blob/master/gif/carrousel02.gif?raw=true)
![image](https://github.com/dalong982242260/AndroidCarrouselLayout/blob/master/gif/carrousel03.gif?raw=true)
![image](https://github.com/dalong982242260/AndroidCarrouselLayout/blob/master/gif/carrousel04.gif?raw=true)
![image](https://github.com/dalong982242260/AndroidCarrouselLayout/blob/master/gif/carrousel05.gif?raw=true)

#How to Use

gradle

        compile 'com.dalong:carrousellayout:1.0.0'   
          
or Maven

        <dependency>
          <groupId>com.dalong</groupId>
          <artifactId>carrousellayout</artifactId>
          <version>1.0.0</version>
          <type>pom</type>
        </dependency>                    

xml:

         <com.dalong.carrousellayout.CarrouselLayout
               android:id="@+id/carrousel"
               app:rotateDirection="anticlockwise"
               app:r="200dp"
               app:rotationTime="3000"
               android:gravity="center"
               android:layout_marginBottom="180dp"
               android:layout_width="match_parent"
               android:layout_height="match_parent">
              <ImageView
                  android:src="@mipmap/image1"
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content" />
              <ImageView
                  android:src="@mipmap/image2"
                  android:layout_width="wrap_content"
                  android:layout_height="wrap_content" />
                  ...
           </com.dalong.carrousellayout.CarrouselLayout>


java:

        CarrouselLayout carrousel= (CarrouselLayout) findViewById(R.id.carrousel);
        carrousel.setR(width/3)//设置R的大小
                 .setAutoRotation(false)//是否自动切换
                 .setAutoRotationTime(1500);//自动切换的时间  单位毫秒


#License

Copyright 2016 dalong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.