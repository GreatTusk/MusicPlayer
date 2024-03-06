@echo off
set JLINK_VM_OPTIONS=
set VLC_PLUGIN_PATH=%USERPROFILE%\MusicPlayerFX\vlcjPlugins\plugins
set DIR=%~dp0
"%DIR%\javaw" %JLINK_VM_OPTIONS% -m com.ch.tusk/com.ch.tusk.main.MusicPlayerFX %*
