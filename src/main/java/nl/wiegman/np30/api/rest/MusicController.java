package nl.wiegman.np30.api.rest;

import nl.wiegman.np30.api.dto.Item;
import nl.wiegman.np30.api.dto.Message;
import nl.wiegman.np30.repository.ItemRepo;
import nl.wiegman.np30.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MusicController {

    @Autowired
    MusicService musicService;

    @Autowired
    ItemRepo itemRepo;

    @RequestMapping(value = "/play-folder/{folder-id}", method = RequestMethod.POST)
    public void playFolderNow(@PathVariable("folder-id") String folderId) throws IOException {
        musicService.playFolderNow(itemRepo.findOne(folderId));
    }

    @RequestMapping(value = "/folder/{folder-id}", method = RequestMethod.GET)
    public Item getFolder(@PathVariable("folder-id") String folderId) {
        nl.wiegman.np30.domain.Item folder = itemRepo.findOne(folderId);

        List<Item> childrenOfFolder = new ArrayList<>();
        List<nl.wiegman.np30.domain.Item> itemsInFolder = itemRepo.findByParentId(folderId);
        for(nl.wiegman.np30.domain.Item child : itemsInFolder) {
            Item item = map(child);
            childrenOfFolder.add(item);
        }

        Item result = map(folder);
        result.setChildren(childrenOfFolder);
        return result;
    }

    @RequestMapping(value = "/play-random-folder", method = RequestMethod.POST)
    public Message randomFolder() throws IOException {
        Message result = new Message();
        result.setMessage(musicService.playRandomFolderNow());
        return result;
    }

    @RequestMapping(value = "/refresh-cache", method = RequestMethod.POST)
    public Message updateLocalDb() throws Exception {
        Message result = new Message();
        musicService.refreshCache();
        result.setMessage("Update started");
        return result;
    }

    @RequestMapping(value = "/refresh-cache", method = RequestMethod.GET)
    public Message updateCacheStatus() throws IOException {
        Message result = new Message();
        result.setMessage(musicService.getRefreshCacheProgressStatus());
        return result;
    }

    private Item map(nl.wiegman.np30.domain.Item item) {
        Item result = new Item();
        result.setTitle(item.getTitle());
        result.setId(item.getId());
        result.setIsContainer(item.isContainer());
        return result;
    }
}
