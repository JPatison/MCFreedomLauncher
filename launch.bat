for /f "usebackq delims=|" %%f in (`dir /b MCFreedomLauncher*.jar ^| sort /r`) do (
echo %%f
java -jar %%f 
goto :end
)
:end
