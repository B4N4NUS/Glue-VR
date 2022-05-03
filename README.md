# GlueVR Server
Server application based on SlimeVR.

# Reimagined UI
In my opinion SlimeVR server is suffering with its non user friendly interface. So, I decided to rearrange and reimagine existing content and add some new features to the app.

# Modular interface
This means that you can open only the windows you need at a certain time to make your own workflow.

# Spot a difference
## Startup window
SlimeVR:

![slime_main](https://user-images.githubusercontent.com/71143870/166464514-8c41a0f3-df83-41b5-8bb0-c2c0d08a127b.png)

GlueVR:

![glue_main](https://user-images.githubusercontent.com/71143870/166464457-0b91f3f8-9901-4784-978f-3b22277fa578.png)

## And now with all avaliable windows opened
SlimeVR:

![slimevr_all_windows](https://user-images.githubusercontent.com/71143870/166464702-6b137e69-41fd-4453-9ca2-7ad3c972ef56.png)

GlueVR:

![gluevr_all_windows](https://user-images.githubusercontent.com/71143870/166464809-dd389370-ed62-4d65-9e27-f27db3c684dc.png)

# New features
## Settings window
All program settings including ui settings, wifi settings and body configuration are presented in Settings window

![settings](https://user-images.githubusercontent.com/71143870/166465147-e9a1fbe5-b259-40ff-ae55-060165d912ec.png)

## Sound notification
If you enable sound notification in UI Settings, you will hear sound of connection and disconnection tracker events.

![sound](https://user-images.githubusercontent.com/71143870/166465306-18b5887b-9c3c-4f74-81c0-7ab4199d5f7e.png)

## Meet the themes customization
With help of Swing library FlatLaf (https://www.formdev.com/flatlaf/) I added wide variety of different themes.

You can find your favorite theme at UI Settings window in list named “Themes”:

![themes](https://user-images.githubusercontent.com/71143870/166465381-804eec2a-25a4-4b8a-a37d-1d9f5a15b68b.png)


### Selecting different themes:

![themes](https://user-images.githubusercontent.com/71143870/166465466-916400c1-20f5-454e-8c95-a526cc59a552.gif)

## Skeleton renderer
Sometimes it’s a real pain in the ass to correctly calibrate your skeleton. But I’ve made a solution!

Now from main server window you can go to Skeleton Renderer:

![render](https://user-images.githubusercontent.com/71143870/166465553-b7a28c61-6b6a-45e3-8bb9-4fd9a9d568bb.gif)

It’s divided for 4 quarters: front view, side view, top-down view and renderer settings.

In settings quarter you can hid/show skeleton joints represented as circles, hid/show labels with names of the joints, enable depth, which is realized by increasing and decreasing joints circles, also you can play with depth and scale multipliers.

![render](https://user-images.githubusercontent.com/71143870/166465693-010f98ef-36a6-4e95-9aa2-8ba0f9f6d0e4.png)

# Reimagined functional
## New trackers list
![list](https://user-images.githubusercontent.com/71143870/166465808-7e726594-18e6-4d96-9aa8-abdaa90926bd.png)

Now trackers list became a table. It has all necessary info and also customizable from UI Settings window. You can hid/show ping, rssi and tps columns.

In addition to this, status in now presented as green/red/transparent button.
	*	green means connected
	*	red means disconnected
	*	transparent means this tracker is virtual
	
Status buttons are clickable, so, if you use GlueVR trackers firmware you can also disable your trackers right through server application.

## Relocation of body configuration section
Now all body configuration section can be found in Body Configuration window. You can customize this window to enable skeleton autoconfiguration, autoconfiguration of SteamVR trackers and also you can hid/show raw skeleton data table. All these interactions could be done from UI Settings window.

Some variants of customization:

![body_config_variants](https://user-images.githubusercontent.com/71143870/166466179-98643490-a7f0-4610-834f-227845056bf7.png)

## Relocation of GUI Zoom button
I’ve kidnapped it and hid far away in UI Settings window.

![zoom](https://user-images.githubusercontent.com/71143870/166466286-e5f22bc1-235c-4514-8276-48565a4c1e47.png)

## Saving/loading config file and logging
Added lots of new info to config. Some of which are current theme, current windows locations, current windows sizes and all info about customization you’ve made throughout current session.

Also, now all files of server app are stored in your AppData folder.

## New mount and designation selection sections
SlimeVR designation selection and mount selection lists:

![slime_des](https://user-images.githubusercontent.com/71143870/166466616-07fdb4eb-d87a-4533-af7c-dd79a7afab09.png)
![slime_mount](https://user-images.githubusercontent.com/71143870/166466622-81e2e8b0-b6cb-45b9-9a6f-36dfe721bbd9.png)

GlueVR designation selection and mount selection popups:

![glue_des](https://user-images.githubusercontent.com/71143870/166466646-50609a7a-6184-485b-a5ce-32786b16a781.png)
![glue_mount](https://user-images.githubusercontent.com/71143870/166466649-4a5434fc-f1f3-4d6c-ab02-e21ddeac42ff.png)


















# License Clarifications

**SlimeVR software** (including server, firmware, drivers, installator, documents, and others - see licence for each case specifically) **is distributed under MIT License and is copyright of Eiren Rain and SlimeVR.** MIT Licence is a permissive license giving you rights to modify and distribute the software with little strings attached.

**However, there are some limits, and if you wish to distribute software based on SlimeVR, you need to be aware of them:**

* When distributing any software based on SlimeVR, you have to clarify to the end user that your software is based on SlimeVR that is distributed under MIT License and is subject to copyright of Eiren Rain
* You must clarify either which parts of original software you're using, or what changes you did to the original software (i.e. clarify which parts of your software is covered by MIT License)
* You must provide a copy of the original license (see LICENSE file)
* You don't have to release your own software under MIT License or even open source at all, but you have to state that it's based on SlimeVR
* This applies even if you distribute software without the source code
#
