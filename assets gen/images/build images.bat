rmdir /s /q "..\..\Colonists Client Android\assets\images"
java -classpath "..\..\Colonists Client Common\libs\gdx.jar";gdx-tools.jar com.badlogic.gdx.tools.texturepacker.TexturePacker sprites\common "..\..\Colonists Client Android\assets\images\sprites" common.pack
java -classpath "..\..\Colonists Client Common\libs\gdx.jar";gdx-tools.jar com.badlogic.gdx.tools.texturepacker.TexturePacker sprites\worldScene "..\..\Colonists Client Android\assets\images\sprites" worldScene.pack
xcopy /s backgrounds "..\..\Colonists Client Android\assets\images\backgrounds\"
pause