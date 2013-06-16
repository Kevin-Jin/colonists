@ECHO off
set CLASSPATH=bin;..\Celdroids Client Common\bin;..\Celdroids Common\bin;libs\gdx-backend-lwjgl-natives.jar;libs\gdx-backend-lwjgl.jar;libs\gdx-natives.jar;libs\sqlite-jdbc-3.7.15-SNAPSHOT-2.jar;..\Celdroids Client Common\libs\gdx.jar
java -Dnet.pjtb.celdroids.client.desktop.state.dir=state net.pjtb.celdroids.client.desktop.DesktopGame
pause
