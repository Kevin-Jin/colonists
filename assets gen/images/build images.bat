rmdir /s /q "..\..\Celdroids Client Android\assets\images"
java -classpath "..\..\Celdroids Client Common\libs\gdx.jar";gdx-tools.jar com.badlogic.gdx.tools.imagepacker.TexturePacker2 sprites "..\..\Celdroids Client Android\assets\images\sprites" sprites.pack
xcopy /s backgrounds "..\..\Celdroids Client Android\assets\images\backgrounds\"
pause