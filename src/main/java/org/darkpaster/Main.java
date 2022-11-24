package org.darkpaster;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.impl.TelegramBotClient;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {

    private static StringBuilder message = new StringBuilder();
    private static Message realMessage;
    private static User user;
    private static Long chatId;

    private static Update update;
    static TelegramBot bot;
    public static void main(String[] args) {
        bot = new TelegramBot("");
       mainListener();
    }


    static void mainListener(){
        bot.setUpdatesListener(updates -> {
            update = updates.get(0);
            chatId = update.message().chat().id();
            String mess = update.message().text();
            realMessage = update.message();
            user = realMessage.from();

            OkHttpClient clien = new OkHttpClient();

            //TelegramBotClient client = new TelegramBotClient(24129443,"e354dae4d8c6280c5c73e0117c1177b7", "adads");


            if(mess.startsWith("/link")){
                parsePage(cutString(mess, "/link "));
            }

            if(mess.startsWith("/group")){
                parseGroup(cutString(mess, "/group ").replaceAll(" ", ""));
            }



            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private static void parseGroup(String mess){
        if(mess.length() < 1){
            newMess("Such group is missing.");
            return;
        }

    }

    private static void parsePage(String mess){
        Document doc = getPage(cutString(mess, "/link "));
        //System.out.println(doc);
        doc.outputSettings().charset("UTF-8");
//                for(Element el: doc.getAllElements()){
//
//                }
        Elements links = doc.select("a[href]");
        Elements media = doc.select("[src]");
        Elements imports = doc.select("link[href]");
        int num = media.size();
        //for(Element el: comments){
        //System.out.println(el);
        //System.out.println(el.getAllElements());
        //}

        //eachText() - текст внутри тегов

        message.append(num).append(" медиа:\n\n");

        for(Element src: media) {
            message.append(src.tagName()).append(": ");
            message.append(src.attr("abs:src")).append(" ");
            if (src.normalName().equals("img")) {
                message.append(src.attr("width"));
                message.append("x");
                message.append(src.attr("height"));
            }
            message.append(" (").append(src.text()).append(")").append("\n");
            checkSize();
        }
        message.append("\n");
        num = links.size();

        message.append(num).append(" ссылок:\n\n");
        for (Element link : links) {
            message.append(link.attr("abs:href")).append(" (").append(link.text()).append(")").append("\n");
            checkSize();
        }
        message.append("\n");
        num = imports.size();

        message.append(num).append(" импортов:\n\n");
        for (Element link : imports) {
            message.append(link.tag()).append(": ").append(link.attr("abs:href")).append(" (").append(link.attr("rel")).append(")").append("\n");
            checkSize();
        }

        sendMess();

        num = doc.getAllElements().eachText().size();

        newMess(num + " текста:\n\n");

        File file = new File("text.txt");
        try {
            FileWriter fileW = new FileWriter("text.txt");
            for(Element text: doc.getAllElements()){
                fileW.write(text.text());
            }
            fileW.flush();
            fileW.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        newMessFile(file);
    }

    private static void checkSize(){
        if(message.length() > 2000){
            sendMess();
            message.delete(0, message.length());
        }
    }

    private static void newMess(String mess){
        SendResponse response = bot.execute(new SendMessage(chatId, mess));
    }

    private static void newMessFile(File file){
        SendResponse response = bot.execute(new SendDocument(chatId, file));
    }

    private static void sendMess(){
            SendResponse response = bot.execute(new SendMessage(chatId, message.toString()));
    }

    static String cutString(String str, String part){
        return str.replaceAll(part, "");
    }


    static Document getPage(String url){
        try {
            return Jsoup.connect(url)
                    .userAgent("Chrome/4.0.249.0 Safari/532.5")
                    .referrer("http://www.google.com")
                    .timeout(5000)
                    .get();
            //return Jsoup.parse(new URL(url).openStream(), "UTF-8", url);
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}