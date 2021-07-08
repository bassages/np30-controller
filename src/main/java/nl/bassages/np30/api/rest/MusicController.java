package nl.bassages.np30.api.rest;

import nl.bassages.np30.api.dto.Message;
import nl.bassages.np30.domain.Item;
import nl.bassages.np30.repository.ItemRepo;
import nl.bassages.np30.service.MusicService;
import nl.bassages.np30.service.PlayBackDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MusicController {

    private final MusicService musicService;
    private final ItemRepo itemRepo;

    @Autowired
    public MusicController(MusicService musicService, ItemRepo itemRepo) {
        this.musicService = musicService;
        this.itemRepo = itemRepo;
    }

    @PostMapping("/play-folder/{folder-id}")
    public void playFolderNow(@PathVariable("folder-id") String folderId) throws IOException {
        final Item folder = itemRepo.findById(folderId).get();
        musicService.playFolderNow(folder);
    }

    @GetMapping( "/folder/{folder-id}")
    public nl.bassages.np30.api.dto.Item getFolder(@PathVariable("folder-id") String folderId) {
        nl.bassages.np30.api.dto.Item result = null;

        Item folder = itemRepo.findById(folderId).get();
        if (folder != null) {
            List<nl.bassages.np30.api.dto.Item> childrenOfFolder = new ArrayList<>();
            List<Item> itemsInFolder = itemRepo.findByParentId(folderId);
            for(Item child : itemsInFolder) {
                nl.bassages.np30.api.dto.Item item = map(child);
                childrenOfFolder.add(item);
            }

            result = map(folder);
            result.setChildren(childrenOfFolder);
        }
        return result;
    }

    @PostMapping("/play-random-folder")
    public Message randomFolder() throws IOException {
        Message result = new Message();
        result.setMessage(musicService.playRandomFolderNow());
        return result;
    }

    @PostMapping( "/refresh-cache")
    public Message updateLocalDb() throws Exception {
        Message result = new Message();
        musicService.refreshCache();
        result.setMessage("Update started");
        return result;
    }

    @GetMapping("/refresh-cache")
    public Message updateCacheStatus() {
        Message result = new Message();
        result.setMessage(musicService.getRefreshCacheProgressStatus());
        return result;
    }

    @PostMapping("/skip-next")
    public Message playNext() throws IOException {
        Message result = new Message();
        result.setMessage(musicService.skipNext());
        return result;
    }

    @PostMapping("/skip-previous")
    public Message skipPrevious() throws IOException {
        Message result = new Message();
        result.setMessage(musicService.skipPrevious());
        return result;
    }

    @GetMapping("/playback-details")
    public PlayBackDetails info() throws Exception {
        return musicService.getPlaybackDetails();
    }

    private nl.bassages.np30.api.dto.Item map(Item item) {
        nl.bassages.np30.api.dto.Item result = new nl.bassages.np30.api.dto.Item();
        result.setTitle(item.getTitle());
        result.setId(item.getId());
        result.setIsContainer(item.isContainer());
        return result;
    }
}
