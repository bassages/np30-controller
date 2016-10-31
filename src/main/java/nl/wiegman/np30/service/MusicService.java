package nl.wiegman.np30.service;

import nl.wiegman.np30.domain.Item;
import nl.wiegman.np30.repository.ItemRepo;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.upnp.schemas.metadata_1_0.didl_lite.ContainerType;
import org.upnp.schemas.metadata_1_0.didl_lite.ItemType;
import org.upnp.schemas.metadata_1_0.didl_lite.RootType;

import javax.transaction.Transactional;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MusicService {

    private static final Logger LOG = LoggerFactory.getLogger(MusicService.class);

    private static final String SERVER_UDN = "e68f7d3a-302b-4bf2-98b9-15c5ad390f0b";

    public static final String NP_30_BASE_URL = "http://np30:8050/" + SERVER_UDN;

    public static final String TOP_ID = "0";

    private static String NAVIGATOR_NAME = "eae7dc9f-ce35-4940-bcd9-f495e4867cf5_NP";
    private String navigatorId;

    private Random randomGenerator = new Random();

    @Autowired
    ItemRepo itemRepo;

    private enum PlaylistAction {
        PLAY_NOW,
        REPLACE
    }

    String isRegisteredNavigatorNameTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                    "<u:IsRegisteredNavigatorName xmlns:u=\"urn:UuVol-com:service:UuVolControl:5\">" +
                        "<NavigatorName>" + NAVIGATOR_NAME + "</NavigatorName>" +
                    "</u:IsRegisteredNavigatorName>" +
                "</s:Body>" +
            "</s:Envelope>";

    String registerNavigatorNameTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                    "<u:RegisterNamedNavigator xmlns:u=\"urn:UuVol-com:service:UuVolControl:5\">" +
                        "<NewNavigatorName>" + NAVIGATOR_NAME + "</NewNavigatorName>" +
                    "</u:RegisterNamedNavigator>" +
                "</s:Body>" +
            "</s:Envelope>";

    String playFolderNowTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                    "<u:QueueFolder xmlns:u=\"urn:UuVol-com:service:UuVolControl:5\">" +
                        "<DIDL>&lt;DIDL-Lite xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\"&gt;&lt;container id=\"%s\" parentID=\"%s\"&lt;upnp:class&gt;object.container.album.musicAlbum&lt;/upnp:class&gt;&lt;/container&gt;&lt;/DIDL-Lite&gt;</DIDL>" +
                        "<ServerUDN>" + SERVER_UDN + "</ServerUDN>" +
                        "<Action>%s</Action>" +
                        "<NavigatorId>%s</NavigatorId>" +
                        "<ExtraInfo></ExtraInfo>" +
                    "</u:QueueFolder>" +
                "</s:Body>" +
            "</s:Envelope>";

    String browseRequestTemplate = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<s:Envelope s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\" xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "<s:Body>" +
                    "<u:Browse xmlns:u=\"urn:schemas-upnp-org:service:ContentDirectory:1\">" +
                        "<ObjectID>%s</ObjectID>" +
                        "<BrowseFlag>BrowseDirectChildren</BrowseFlag>" +
                        "<Filter>*</Filter>" +
                        "<StartingIndex>0</StartingIndex>" +
                        "<RequestedCount>1500</RequestedCount>" +
                        "<SortCriteria></SortCriteria>" +
                    "</u:Browse>" +
                "</s:Body>" +
            "</s:Envelope>";

    private static ArrayList<String> EXCLUDE_CONTAINERS_WITH_NAME = new ArrayList<>();
    static {
        // EXCLUDE_CONTAINERS_WITH_NAME.add("POP");
//        EXCLUDE_CONTAINERS_WITH_NAME.add("ROCK");
//        EXCLUDE_CONTAINERS_WITH_NAME.add("JAZZY");
//        EXCLUDE_CONTAINERS_WITH_NAME.add("BASS");
//        EXCLUDE_CONTAINERS_WITH_NAME.add("DISCO");

        EXCLUDE_CONTAINERS_WITH_NAME.add("COVER");
        EXCLUDE_CONTAINERS_WITH_NAME.add("COVERS");
        EXCLUDE_CONTAINERS_WITH_NAME.add("ARTWORK");
        EXCLUDE_CONTAINERS_WITH_NAME.add("SCAN");
        EXCLUDE_CONTAINERS_WITH_NAME.add("SCANS");
        EXCLUDE_CONTAINERS_WITH_NAME.add("COVERART");

        EXCLUDE_CONTAINERS_WITH_NAME.add("MOVIES");
        EXCLUDE_CONTAINERS_WITH_NAME.add("3DMOVIES");
        EXCLUDE_CONTAINERS_WITH_NAME.add("MUSICMOVIES");
        EXCLUDE_CONTAINERS_WITH_NAME.add("$RECYCLE.BIN");
        EXCLUDE_CONTAINERS_WITH_NAME.add("XBMC_BOXEE");
        EXCLUDE_CONTAINERS_WITH_NAME.add("SYSTEM VOLUME INFORMATION");
    }

    @Transactional
    public String playRandomFolderNow() throws IOException {
        registerNavigatorWhenNotAlreadyDone();

        List<Item> items = itemRepo.findByIsContainerFalse();

        if (items.size() > 0) {
            int randomIndex = randomGenerator.nextInt(items.size());
            Item randomItem = items.get(randomIndex);
            Item randomContainer = getParent(randomItem);

            playFolderNow(randomContainer);

            return getPathString(randomContainer);
        }
        return "Failed to select random item";
    }

    private String getPathString(Item item) throws IOException {
        List<Item> path = getPathTo(new ArrayList<>(), item);

        Collections.reverse(path);

        String randomItemPathString = "";
        for (int i=0; i<path.size(); i++) {
            Item pathItem = path.get(i);
            if (i > 0) {
                randomItemPathString += " -> ";
            }
            browse(pathItem.getId());

            randomItemPathString += pathItem.getTitle();
        }

        String response = queueFolder(item, PlaylistAction.REPLACE, navigatorId);
        if (!getElementContentXml(response, "Result").equals("OK")) {
            throw new RuntimeException("Failed to queueFolder. Response: " + response);
        }
        return randomItemPathString;
    }

    public void playFolderNow(Item folder) throws IOException {
        List<Item> path = getPathTo(new ArrayList<>(), folder);

        Collections.reverse(path);

        for (int i=0; i<path.size(); i++) {
            Item pathItem = path.get(i);
            browse(pathItem.getId());
        }

        String response = queueFolder(folder, PlaylistAction.REPLACE, navigatorId);
        if (!getElementContentXml(response, "Result").equals("OK")) {
            throw new RuntimeException("Failed to queueFolder. Response: " + response);
        }
    }

    private List<Item> getPathTo(List<Item> path, Item item) {
        if (item != null) {
            path.add(item);
            if (!item.getId().equals(TOP_ID)) {
                getPathTo(path, getParent(item));
            }
        }
        return path;
    }

    private Item getParent(Item item) {
        return itemRepo.findOne(item.getParentId());
    }

    private List<Item> getChildren(Item item) {
        return itemRepo.findByParentId(item.getId());
    }

    private void registerNavigatorWhenNotAlreadyDone() throws IOException {
        String isRegisteredNavigatorNameResponse = isRegisteredNavigatorName();
        navigatorId = getElementContentXml(isRegisteredNavigatorNameResponse, "RetNavigatorId");

        if (navigatorId == null) {
            String registerNavigatorNameResponse = registerNavigatorName();
            navigatorId = getElementContentXml(registerNavigatorNameResponse, "RetNavigatorId");

            if (navigatorId == null) {
                throw new RuntimeException("Failed to determine navigatorId. SoapResponse: " + isRegisteredNavigatorNameResponse);
            }
        }
    }

    @Async
    public void updateLocalDb() throws Exception {
        long start = System.currentTimeMillis();

        registerNavigatorWhenNotAlreadyDone();

        itemRepo.deleteAll();

        Item item = new Item();
        item.setId(TOP_ID);
        item.setTitle("Music");

        browse(item, 0); // Always start from top, otherwise an error will be returned

        LOG.info("Processing took " + DurationFormatUtils.formatDurationHMS(System.currentTimeMillis() - start));
    }

    private void browse(Item item, int level) throws Exception {
        String responseString = browse(item.getId());

        if (responseString.contains("Fault") && responseString.contains("faultstring")) {
            LOG.error(responseString);
        }

        String resultXml = getElementContentXml(responseString, "Result");

        JAXBContext jaxbContext = JAXBContext.newInstance(RootType.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        StringReader reader = new StringReader(resultXml);
        RootType rootType = ((JAXBElement<RootType>) unmarshaller.unmarshal(reader)).getValue();

        for(Object o : rootType.getAllowedUnderDIDLLite()) {
            if (o instanceof ContainerType) {
                ContainerType container = (ContainerType) o;

                String title = container.getTitle().getValue();

                if (!EXCLUDE_CONTAINERS_WITH_NAME.contains(title.toUpperCase())) {
                    Item savedContainer = save(container.getId(), container.getParentID(), title, level, true);
                    browse(savedContainer, level + 1);
                }

            } else if (o instanceof ItemType) {
                ItemType itemType = (ItemType) o;
                save(itemType.getId(), itemType.getParentID(), itemType.getTitle().getValue(), level, false);
            }
        }
    }

    private String getElementContentXml(String soapResponse, String elementName) {
        String result = null;

        final Pattern pattern = Pattern.compile(".+<" + elementName + ">(.+)</" + elementName + ">.+", Pattern.DOTALL);
        final Matcher matcher = pattern.matcher(soapResponse);
        if (matcher.matches()) {
            result = StringEscapeUtils.unescapeXml(matcher.group(1));
        }
        return result;
    }

    private String isRegisteredNavigatorName() throws IOException {
        return executeSoapAction("/RecivaRadio/invoke", "\"urn:UuVol-com:service:UuVolControl:5#IsRegisteredNavigatorName\"", isRegisteredNavigatorNameTemplate);
    }

    private String registerNavigatorName() throws IOException {
        return executeSoapAction("/RecivaRadio/invoke", "\"urn:UuVol-com:service:UuVolControl:5#RegisterNamedNavigator\"", registerNavigatorNameTemplate);
    }

    private String browse(String id) throws IOException {
        return executeSoapAction("/ContentDirectory/invoke", "\"urn:schemas-upnp-org:service:ContentDirectory:1#Browse\"", String.format(browseRequestTemplate, id));
    }

    private String queueFolder(Item folder, PlaylistAction playlistAction, String navigatorId) throws IOException {
        return executeSoapAction("/RecivaRadio/invoke", "\"urn:UuVol-com:service:UuVolControl:5#QueueFolder\"", String.format(playFolderNowTemplate, folder.getId(), folder.getParentId(), playlistAction.name(), navigatorId));
    }
    private String executeSoapAction(String port, String soapAction, String body) throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(NP_30_BASE_URL + port);
        httpPost.setHeader("Content-Type", "text/xml; charset=\"utf-8\"");
        httpPost.setHeader("SOAPAction", soapAction);
        httpPost.setEntity(new StringEntity(body));

        String responseString;
        try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            HttpEntity entity = response.getEntity();
            responseString = EntityUtils.toString(entity);
        }
        return responseString;
    }

    private Item save(String id, String parentId, String title, int level, boolean isContainer) {
        Item item = new Item();
        item.setId(id);
        item.setTitle(title);
        item.setParentId(parentId);
        item.setIsContainer(isContainer);

        String s = "";
        for (int i=0; i<level; i++) {
            s += "|-";
        }
        s += title;

        itemRepo.save(item);
        LOG.info(s);

        return item;
    }
}
