@ECHO off
set CLASSPATH=bin;..\Colonists Client Common\bin;..\Colonists Common\bin;libs\gdx-backend-lwjgl-natives.jar;libs\gdx-backend-lwjgl.jar;libs\gdx-natives.jar;libs\sqlite-jdbc-3.7.15-SNAPSHOT-2.jar;..\Colonists Client Common\libs\gdx.jar
java -Din.kevinj.colonists.client.desktop.state.dir=state in.kevinj.colonists.client.desktop.DesktopGame
pause
