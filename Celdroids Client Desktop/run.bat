@ECHO off
set CLASSPATH=bin;..\Celdroids Client Common\bin;..\Celdroids Common\bin;libs\gdx-backend-lwjgl-natives.jar;libs\gdx-backend-lwjgl.jar;libs\gdx-natives.jar;..\Celdroids Client Common\libs\gdx.jar
java net.pjtb.celdroids.client.desktop.DesktopGame
pause
