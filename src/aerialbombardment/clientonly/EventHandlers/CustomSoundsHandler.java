package aerialbombardment.clientonly.eventhandlers;

import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.event.ForgeSubscribe;

public class CustomSoundsHandler
{
  @ForgeSubscribe
  public void onSound(SoundLoadEvent event)
  {
    event.manager.addSound("testframework:MultiBlockPlace.ogg");
  }
}

