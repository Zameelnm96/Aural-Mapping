# Aural Mapping

This project about  Wireless Sensor Network based Real-time Noise Detection and Noise Monitoring on Urban areas and cities. 

## abstract

Monitoring environmental parameters is an area of research that has attracted scientific attention 
during the last decades. The concept of smart cities often needs information including variables 
like CO2 levels, Water quality, Air quality or Noise levels. Monitoring these environmental 
parameters in large areas over a longer period of time is an expensive and complex process. But 
with the evolving of low-cost and low-power devices which is capable of doing complex tasks, 
has opened a wide area for researchers to develop monitoring devices to deploying a Wireless 
Sensor Network(WSN). The information gathered from these monitoring devices can be used in 
urban city controlling and planning and also it could help citizens. In this work, we described the 
prototyping of low-cost and low-power Raspberry pi sensor node based WSN, how we can 
analyze noise levels in urban areas and cities in real-time. Node devices are connected with 
google cloud to share and process this information in real-time. Processed data can be obtained in 
real-time by relevant administrations (Eg: Central Environmental Authority) and can analyse the 
data. , Also a mobile app is developed for the public users to visualize areas where noise 
pollution is high and be aware of them. //include information about the pilot work.

## Mobile App
User have mobile App interface which track location of  the user always even app is not running. App fetch information about sound source(such as location and radius of the Dangerzone and Warning zone) mark on the google map like below.

![Screenshot_20191018-150214](https://user-images.githubusercontent.com/47120059/67160347-64d24f00-f36d-11e9-90ea-48e4bc39813e.png)
In this picture inner circle idicate the danger zone and outer circle indicate the warining zone. If any user comes
into the zone(warning or danger) app will notify the user like below

![Screenshot_20191018-150222](https://user-images.githubusercontent.com/47120059/67160399-efb34980-f36d-11e9-9d9d-eb8509272e0a.png)
by tapping the notification user can navigate into map.

The bigest advantage of this app is user don't want to always run the app, It is enough ones he run the app.
There is backround service in the app which run always even after killing the app or restart the phone.
