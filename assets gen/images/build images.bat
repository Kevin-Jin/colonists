rmdir /s /q "..\..\Colonists Client Android\assets\images"
java -classpath "..\..\Colonists Client Common\libs\gdx.jar";gdx-tools.jar com.badlogic.gdx.tools.imagepacker.TexturePacker2 sprites "..\..\Colonists Client Android\assets\images\sprites" sprites.pack
xcopy /s backgrounds "..\..\Colonists Client Android\assets\images\backgrounds\"
pause