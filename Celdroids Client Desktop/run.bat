@ECHO off
set CLASSPATH=bin;..\Celdroids Client Common\bin;..\Celdroids Common\bin;lib\gdx-backend-lwjgl-natives.jar;lib\gdx-backend-lwjgl.jar;lib\gdx-natives.jar;..\Celdroids Client Common\libs\gdx.jar
java net.pjtb.celdroids.client.desktop.DesktopGame
pause
