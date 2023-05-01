package com.example.spring_bot.service;

import com.example.spring_bot.config.BotConfig;
import com.example.spring_bot.model.*;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.example.spring_bot.model.User;
import com.example.spring_bot.model.UserRepository;


@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellersRepository sellersRepository;

    @Autowired
    private AdsRepository adsRepository;
    final BotConfig config;

    static final String HELP_TEXT = "Когда-то тут что-то будет";

    private boolean bCheckForAnswer;
    private String Currency;

    private int CurrentPage=0;

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listofCommands = new ArrayList<>();
        listofCommands.add(new BotCommand("/start","get a welcome message"));
        listofCommands.add(new BotCommand("/mydata","get your data stored"));
        listofCommands.add(new BotCommand("/deletedata","delete my data"));
        listofCommands.add(new BotCommand("/help", "info how to use this bot"));
        listofCommands.add(new BotCommand("/settings","set our preferences"));
        listofCommands.add(new BotCommand("/rates","Переглянути курс основних валют"));
        try {
            this.execute(new SetMyCommands(listofCommands,new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e){
            System.out.println("Pizda");
        }
    }

    private String marketplace = EmojiParser.parseToUnicode("Перейти на марктеплейс"+":money_with_wings:");
    private String profile = EmojiParser.parseToUnicode("Особистий кабінет"+":bust_in_silhouette:");
    private String becomeSeller = EmojiParser.parseToUnicode("Стати продавцем"+":moneybag:");
    private String back = EmojiParser.parseToUnicode("Назад"+":back:");
    private String bot;
    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }
    @Override
    public void onUpdateReceived(Update update) {
        if(bCheckForAnswer)
        {
            if (update.hasMessage() && update.getMessage().hasText()){
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();
                CalculateCurrency(messageText,chatId);
            }
            return;
        }
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            if(messageText.matches(marketplace))
            {
                ShowMarketplaceOptions("Що ви бажаєте зробити?",chatId);
                return;
            }
            if(messageText.matches(profile))
            {
                SendProfileInfo("",chatId);
                return;
            }
            if(messageText.matches(becomeSeller))
            {
                BecomeSeller("",chatId);
                return;
            }
            if(messageText.matches(back))
            {
                switch (CurrentPage){
                    case 2:
                        ShowStartOptions("Що ви бажаєте зробити?",chatId);
                        CurrentPage=1;
                        break;
                    case 3:
                        ShowMarketplaceOptions("Що ви бажаєте зробити?",chatId);
                        CurrentPage=2;
                        break;
                    default:
                        break;
                }
                return;
            }
            switch (messageText) {
                case "/start":
                    registerUser(update.getMessage());
                    ShowStartOptions("Що ви бажаєте зробити?",chatId);
                    break;
                case "/help":
                    sendMessage(chatId,HELP_TEXT);
                    break;
                case "/rates":
                    List<String> currencyCodes = Arrays.asList("USD", "EUR", "GBP", "JPY", "CHF", "CNY");
                    String exchangeRates = null;
                    try {
                        exchangeRates = ExchangeRateParser.getExchangeRates(currencyCodes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sendMessage(chatId, exchangeRates);
                    break;
                case "/mydata":
                    SendData(chatId);
                    break;
                case "Перейти на марктеплейс":
                    ShowMarketplaceOptions("Що ви бажаєте зробити?",chatId);
                    break;
                case "Назад":
                    ShowStartOptions("Що ви бажаєте зробити?",chatId);
                    break;
                case "Отримати курс валюти":
                    ShowGraphOptions("Курс якої валюти ви бажаєте отримати?",chatId);
                    break;
                case "USD":
                    /*try {
                        ("USD",chatId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/
                    SendConvertedData("USD",chatId);
                    break;
                case "EUR":
                    /*try {
                        SendRateGraph("EUR",chatId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/
                    SendConvertedData("EUR",chatId);
                    break;
                case "JPY":
                    /*try {
                        SendRateGraph("JPY",chatId);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }*/
                    SendConvertedData("JPY",chatId);
                    break;
                default:sendMessage(chatId, "Вибачте, але я не знаю такої команди");

            }
        }
        else if(update.hasMessage() && update.getMessage().hasLocation())
        {
            long chatId = update.getMessage().getChatId();
            Location location = update.getMessage().getLocation();


            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            BankFinder bankFinder = new BankFinder();
            String bankInfo = bankFinder.FindNearestBank(latitude,longitude);
            String [] bankInfoArr = bankInfo.split("!");
            sendMessage(chatId,bankInfoArr[0]);

            SendLocation sendLocation = new SendLocation();
            sendLocation.setChatId(update.getMessage().getChatId());
            sendLocation.setLatitude(Double.parseDouble(bankInfoArr[1]));
            sendLocation.setLongitude(Double.parseDouble(bankInfoArr[2]));

            try {
                execute(sendLocation);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void registerUser(Message msg) {

        if(userRepository.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

            userRepository.save(user);

        }
    }
    private void sendMessage(long chatId, String textToSend){
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        try {
            execute(message);
        }
        catch (TelegramApiException e){
            System.out.println("Pizda");
        }


    }

    /*@Scheduled(cron = "0 * * * * *")
    private void SendAds(){
        var ads = adsRepository.findAll();
        var users = userRepository.findAll();
        for(Ads ad : ads)
        {
            for(User user : users)
            {
                sendMessage(user.getChatId(),ad.getAd());
            }
        }
    }*/

    private void SendData(long chatId){
        var findedUser = userRepository.findById(chatId);
        User user = findedUser.get();
        String messge = "Dear, " + user.getFirstName() + " you have registered on" + user.getRegisteredAt();
        sendMessage(chatId, messge);
    }
    private void ShowStartOptions(String message, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        // Создаем список кнопок и добавляем их в разметку
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardButton buttonLocation = new KeyboardButton(EmojiParser.parseToUnicode("Знайти найближче відділення банку"+":bank:"));
        buttonLocation.setRequestLocation(true);
        row1.add(buttonLocation);
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("Перейти на марктеплейс"+":money_with_wings:")));
        row3.add(new KeyboardButton(EmojiParser.parseToUnicode("Отримати курс валюти"+":chart_with_upwards_trend:")));
        row3.add(new KeyboardButton( EmojiParser.parseToUnicode("Валютний калькулятор"+":heavy_plus_sign:"+":heavy_minus_sign:")));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        // Устанавливаем разметку в сообщении
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        markup.setSelective(true);
        sendMessage.setReplyMarkup(markup);

        // Отправляем сообщение с кнопками пользователю
        try {
            execute(sendMessage);
            CurrentPage=1;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void ShowMarketplaceOptions(String message, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        // Создаем список кнопок и добавляем их в разметку
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add(new KeyboardButton(EmojiParser.parseToUnicode("Купити валюту"+":inbox_tray:")));
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("Продати валюту"+":outbox_tray:")));
        row3.add(new KeyboardButton(EmojiParser.parseToUnicode("Особистий кабінет"+":bust_in_silhouette:")));
        row4.add(new KeyboardButton(EmojiParser.parseToUnicode("Назад"+":back:")));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);
        keyboard.add(row4);

        // Устанавливаем разметку в сообщении
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        markup.setSelective(true);
        sendMessage.setReplyMarkup(markup);

        // Отправляем сообщение с кнопками пользователю
        try {
            execute(sendMessage);
            CurrentPage=2;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void SendProfileInfo(String message, long chatId)
    {
        Optional<User> user = userRepository.findById(chatId);
        User userUser = user.get();
        String messageToSend = "Ваш профіль:\nІм'я:"+userUser.getFirstName()+"\nДата реєстрації:"+userUser.getRegisteredAt();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageToSend);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        if(userUser.isBIsSeller())
        {
            row1.add(new KeyboardButton(EmojiParser.parseToUnicode("Зупинити продаж"+":x:")));
        }
        else
        {
            row1.add(new KeyboardButton(EmojiParser.parseToUnicode("Стати продавцем"+":moneybag:")));
        }
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("Поповнити баланс"+":inbox_tray:")));
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("Вивести гроші"+":outbox_tray:")));
        row3.add(new KeyboardButton(EmojiParser.parseToUnicode("Назад"+":back:")));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        // Устанавливаем разметку в сообщении
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        markup.setSelective(true);
        sendMessage.setReplyMarkup(markup);

        try {
            execute(sendMessage);
            CurrentPage=3;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void BecomeSeller(String message, long chatId)
    {
        Optional<User> user = userRepository.findById(chatId);
        User userUser = user.get();
        String messageToSend = "Ви стали продавцем";
        userUser.setBIsSeller(true);
        userRepository.save(userUser);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(messageToSend);

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        if(userUser.isBIsSeller())
        {
            row1.add(new KeyboardButton(EmojiParser.parseToUnicode("Зупинити продаж"+":x:")));
        }
        else
        {
            row1.add(new KeyboardButton(EmojiParser.parseToUnicode("Стати продавцем"+":moneybag:")));
        }
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("Поповнити баланс"+":inbox_tray:")));
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("Вивести гроші"+":outbox_tray:")));
        row3.add(new KeyboardButton(EmojiParser.parseToUnicode("Назад"+":back:")));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        markup.setSelective(true);
        sendMessage.setReplyMarkup(markup);

        try {
            execute(sendMessage);
            CurrentPage=3;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void ShowGraphOptions(String message, long chatId)
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        // Создаем список кнопок и добавляем их в разметку
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        row1.add(new KeyboardButton(EmojiParser.parseToUnicode("USD"+":us:")));
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("EUR"+":eu:")));
        row3.add(new KeyboardButton(EmojiParser.parseToUnicode("JPY"+":jp:")));
        keyboard.add(row1);
        keyboard.add(row2);
        keyboard.add(row3);

        // Устанавливаем разметку в сообщении
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setKeyboard(keyboard);
        markup.setResizeKeyboard(true);
        markup.setOneTimeKeyboard(true);
        markup.setSelective(true);
        sendMessage.setReplyMarkup(markup);

        // Отправляем сообщение с кнопками пользователю
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void SendRateGraph(String message, long chatId) throws IOException {
        ChartCreator chartCreator = new ChartCreator();
        chartCreator.GenerateChart(message);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);

        // Указываем файл изображения
        File image = new File("chart.png");
        InputFile inputFile = new InputFile(image);
        sendPhoto.setPhoto(inputFile);

        try {
            execute(sendPhoto);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void SendConvertedData(String message, long chatId)
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Введите кол-во вашей валюты:");
        bCheckForAnswer = true;
        Currency=message;
        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    private void CalculateCurrency(String userMessage,long chatId ){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String answer = CurrencyCalculator.convertCurrency(Double.parseDouble(userMessage),Currency);
        sendMessage.setText("Кол-во в "+Currency+": "+answer);
        bCheckForAnswer=false;
        Currency=null;
        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    void SellCurrency(Message msg)
    {

        if(sellersRepository.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();
            var chat = msg.getChat();

            Sellers seller = new Sellers();
            seller.setChatId(chatId);

            sellersRepository.save(seller);


        }
    }
}