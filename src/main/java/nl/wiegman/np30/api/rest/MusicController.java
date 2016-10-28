package nl.wiegman.np30.api.rest;

import nl.wiegman.np30.api.dto.Item;
import nl.wiegman.np30.repository.ItemRepo;
import nl.wiegman.np30.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @RequestMapping(value = "/folder", method = RequestMethod.GET)
    public List<Item> getFolders() {
        nl.wiegman.np30.domain.Item root = itemRepo.findOne("0:0");
        return buildTree(root);
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

    @RequestMapping(value = "/play-random-folder", method = RequestMethod.GET)
    public String randomFolder() throws IOException {
        return musicService.playRandomFolder();
    }

    @RequestMapping(value = "/update-local-db", method = RequestMethod.GET)
    public String updateLocalDb() throws Exception {
        musicService.updateLocalDb();
        return "Done";
    }
}
