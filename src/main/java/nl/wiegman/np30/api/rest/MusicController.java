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
import java.util.Collections;
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

    @RequestMapping(value = "/files-in-folder/{folder-id}", method = RequestMethod.GET)
    public List<Item> filesInFolder(@PathVariable("folder-id") String folderId) {
        List<Item> result = new ArrayList<>();

        List<nl.wiegman.np30.domain.Item> filesInFolder = itemRepo.findByParentIdAndIsContainerFalse(folderId);

        for(nl.wiegman.np30.domain.Item fileInFolder : filesInFolder) {
            Item fileInFolderDto = map(fileInFolder);
            result.add(fileInFolderDto);
        }
        return result;
    }

    @RequestMapping(value = "/folder", method = RequestMethod.GET)
    public List<Item> getFolders() {
        nl.wiegman.np30.domain.Item root = itemRepo.findOne("0:0");
        if (root != null) {
            return buildTree(root);
        } else {
            return Collections.emptyList();
        }
    }

    @RequestMapping(value = "/play-random-folder", method = RequestMethod.POST)
    public Message randomFolder() throws IOException {
        Message result = new Message();
        result.setMessage(musicService.playRandomFolderNow());
        return result;
    }

    @RequestMapping(value = "/update-local-db", method = RequestMethod.POST)
    public Message updateLocalDb() throws Exception {
        Message result = new Message();
        musicService.updateLocalDb();
        result.setMessage("Update started");
        return result;
    }

    private List<Item> buildTree(nl.wiegman.np30.domain.Item root) {
        List<Item> result = new ArrayList<>();

        List<nl.wiegman.np30.domain.Item> children = itemRepo.findByParentIdAndIsContainerTrue(root.getId());
        for(nl.wiegman.np30.domain.Item child : children) {
            Item childDto = map(child);
            result.add(childDto);
            childDto.setChildren(buildTree(child));
        }
        return result;
    }

    private Item map(nl.wiegman.np30.domain.Item item) {
        Item result = new Item();
        result.setTitle(item.getTitle());
        result.setId(item.getId());
        return result;
    }
}
