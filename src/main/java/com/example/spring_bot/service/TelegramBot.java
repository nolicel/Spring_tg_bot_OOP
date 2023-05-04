package com.example.spring_bot.service;

import com.example.spring_bot.config.BotConfig;
import com.example.spring_bot.model.*;
import com.vdurmont.emoji.EmojiParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

import com.example.spring_bot.model.User;
import com.example.spring_bot.model.UserRepository;

enum Pages{
    MAIN,
    MARKETPLACE,
    PROFILE,
    PROFILE_CURRENCY_TOP,
    PROFILE_CURRENCY_WITHDRAW,
    CALCULATOR_CURRENCY,
    GRAPHS_CURRENCY,
    BUY_CURRENCY,
    SELL_CURRENCY
}
@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellersRepository sellersRepository;

    @Autowired
    private AdsRepository adsRepository;

    @Autowired
    private WalletsRepository walletsRepository;

    final BotConfig config;

    static final String HELP_TEXT = "Когда-то тут что-то будет";

    private boolean bCheckForAnswer;
    private String Currency;
    private long SellerID;
    private long BuyerID;
    private Pages CurrentPage;

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
    private String stopSelling = EmojiParser.parseToUnicode("Зупинити продаж"+":x:");
    private String buyCurrency = EmojiParser.parseToUnicode("Купити валюту"+":inbox_tray:");
    private String addMoney=EmojiParser.parseToUnicode("Поповнити баланс"+":inbox_tray:");
    private String withdrawMoney =EmojiParser.parseToUnicode("Вивести гроші"+":outbox_tray:");
    private String usdText = EmojiParser.parseToUnicode("USD"+":us:");
    private String eurText = EmojiParser.parseToUnicode("EUR"+":eu:");
    private String jpyText = EmojiParser.parseToUnicode("JPY"+":jp:");
    private String uahText = EmojiParser.parseToUnicode("UAH"+":ua:");
    private String calculator = EmojiParser.parseToUnicode("Валютний калькулятор"+":heavy_plus_sign:"+":heavy_minus_sign:");
    private String graphs = EmojiParser.parseToUnicode("Отримати курс валюти"+":chart_with_upwards_trend:");
    private String sellCurrncy = EmojiParser.parseToUnicode("Продати валюту"+":outbox_tray:");
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
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            String[] parts = data.split(" - ");
            BuyerID = Long.parseLong(parts[0]);
            SellerID = Long.parseLong(parts[1]);
            //AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            //answerCallbackQuery.setText("Яку кількість валюти ви хочете купити?");
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(callbackQuery.getMessage().getChatId());
            sendMessage.setText("Яку кількість валюти ви хочете купити?");
            try {
                bCheckForAnswer=true;
                execute(sendMessage);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
        if(bCheckForAnswer)
        {
            if(update.hasMessage() && update.getMessage().hasText()){
                String messageText = update.getMessage().getText();
                long chatId = update.getMessage().getChatId();

                if(CurrentPage== Pages.PROFILE_CURRENCY_TOP)
                {
                    AddMoney(Currency,messageText,chatId);
                    bCheckForAnswer=false;
                    return;
                }
                if(CurrentPage==Pages.CALCULATOR_CURRENCY)
                {
                    CalculateCurrency(Currency,messageText,chatId);
                    bCheckForAnswer=false;
                    return;
                }
                if(CurrentPage==Pages.PROFILE_CURRENCY_WITHDRAW)
                {
                    WithdrawMoney(Currency,messageText,chatId);
                    bCheckForAnswer=false;
                    return;
                }
                if(CurrentPage==Pages.BUY_CURRENCY)
                {
                    MakeTrade(BuyerID,SellerID,messageText);
                    bCheckForAnswer=false;
                    return;
                }
                if(CurrentPage==Pages.SELL_CURRENCY)
                {
                    SetTradeRate(chatId,messageText);
                    bCheckForAnswer=false;
                    return;
                }
            }
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
                SendProfileInfo(chatId);
                return;
            }
            if(messageText.matches(becomeSeller))
            {
                BecomeSeller(chatId);
                return;
            }
            if(messageText.matches(stopSelling))
            {
                StopSelling(chatId);
                return;
            }
            if(messageText.matches(addMoney))
            {
                CurrentPage=Pages.PROFILE_CURRENCY_TOP;
                SendCurrencyOption("Яку валюту ви хочете поповнити?",chatId);
                return;
            }
            if(messageText.matches(calculator))
            {
                CurrentPage=Pages.CALCULATOR_CURRENCY;
                SendCurrencyOption("Яку валюту ви хочете порахувати?",chatId);
                return;
            }
            if(messageText.matches(graphs))
            {
                CurrentPage=Pages.GRAPHS_CURRENCY;
                SendCurrencyOption("Курс якої валюти бажаєте отримати?",chatId);
                return;
            }
            if(messageText.matches(withdrawMoney))
            {
                CurrentPage=Pages.PROFILE_CURRENCY_WITHDRAW;
                SendCurrencyOption("Яку валюту ви хочете зняти?",chatId);
                return;
            }
            if(messageText.matches(buyCurrency))
            {
                CurrentPage=Pages.BUY_CURRENCY;
                SendCurrencyOption("Яку валюту ви хочете купити?",chatId);
                return;
            }
            if(messageText.matches(sellCurrncy))
            {
                CurrentPage=Pages.SELL_CURRENCY;
                SendCurrencyOption("Яку валюту ви хочете продавати?",chatId);
                return;
            }
            if(messageText.matches(back))
            {
                switch (CurrentPage){
                    case MARKETPLACE:
                        ShowStartOptions("Що ви бажаєте зробити?",chatId);
                        CurrentPage=Pages.MAIN;
                        break;
                    case PROFILE:
                        ShowMarketplaceOptions("Що ви бажаєте зробити?",chatId);
                        CurrentPage=Pages.MARKETPLACE;
                        break;
                    case PROFILE_CURRENCY_TOP:
                        SendProfileInfo(chatId);
                        CurrentPage=Pages.PROFILE;
                        break;
                    case PROFILE_CURRENCY_WITHDRAW:
                        SendProfileInfo(chatId);
                        CurrentPage=Pages.PROFILE;
                        break;
                    case CALCULATOR_CURRENCY:
                        ShowStartOptions("Що ви бажаєте зробити?",chatId);
                        CurrentPage=Pages.MAIN;
                        break;
                    case GRAPHS_CURRENCY:
                        ShowStartOptions("Що ви бажаєте зробити?",chatId);
                        CurrentPage=Pages.MAIN;
                        break;
                    default:
                        break;
                }
                return;
            }
            if(messageText.matches(usdText))
            {
                SendConvertedData("USD",chatId,update);
                return;
            }
            if(messageText.matches(eurText))
            {
                SendConvertedData("EUR",chatId,update);
                return;
            }
            if(messageText.matches(jpyText))
            {
                SendConvertedData("JPY",chatId,update);
                return;
            }
            if(messageText.matches(uahText))
            {
                SendConvertedData("UAH",chatId,update);
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
        if(walletsRepository.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();

            Wallet wallet = new Wallet();
            wallet.setId(chatId);
            walletsRepository.save(wallet);
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
            CurrentPage=Pages.MAIN;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void ShowMarketplaceOptions(String message, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        var userOpt=userRepository.findById(chatId);
        User user = userOpt.get();
        // Создаем список кнопок и добавляем их в разметку
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        row1.add(new KeyboardButton(EmojiParser.parseToUnicode("Купити валюту"+":inbox_tray:")));
        if(user.isBIsSeller())
        {
            row2.add(new KeyboardButton(EmojiParser.parseToUnicode("Продати валюту"+":outbox_tray:")));
        }
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
            CurrentPage=Pages.MARKETPLACE;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void SendProfileInfo(long chatId)
    {
        Optional<User> user = userRepository.findById(chatId);
        User userUser = user.get();

        Optional<Wallet> walletOptional = walletsRepository.findById(chatId);
        Wallet wallet = walletOptional.get();
        String messageToSend = wallet.GetBalance();
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
            CurrentPage=Pages.PROFILE;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void BecomeSeller(long chatId)
    {
        Optional<User> user = userRepository.findById(chatId);
        User userUser = user.get();
        String messageToSend = "Ви стали продавцем";
        userUser.setBIsSeller(true);
        userRepository.save(userUser);
        registerSeller(chatId);
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
            CurrentPage=Pages.PROFILE;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void StopSelling(long chatId)
    {
        Optional<User> user = userRepository.findById(chatId);
        User userUser = user.get();
        String messageToSend = "Ви припинили продаж";
        userUser.setBIsSeller(false);
        removeSeller(chatId);
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
            CurrentPage=Pages.PROFILE;
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void SendCurrencyOption(String message, long chatId)
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        // Создаем список кнопок и добавляем их в разметку
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow row0 = new KeyboardRow();
        KeyboardRow row1 = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        KeyboardRow row4 = new KeyboardRow();
        if(CurrentPage==Pages.PROFILE_CURRENCY_TOP ||CurrentPage==Pages.PROFILE_CURRENCY_WITHDRAW )
        {
            row0.add(new KeyboardButton(EmojiParser.parseToUnicode("UAH"+":ua:")));
        }
        row1.add(new KeyboardButton(EmojiParser.parseToUnicode("USD"+":us:")));
        row2.add(new KeyboardButton(EmojiParser.parseToUnicode("EUR"+":eu:")));
        row3.add(new KeyboardButton(EmojiParser.parseToUnicode("JPY"+":jp:")));
        row4.add(new KeyboardButton(back));
        keyboard.add(row0);
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
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void WithdrawMoney(String currency,String amount, long chatId)
    {
        Optional<Wallet> walletOptional = walletsRepository.findById(chatId);
        Wallet wallet = walletOptional.get();
        double value = Double.parseDouble(amount);
        boolean result = wallet.WithdrawMoney(currency,value);
        walletsRepository.save(wallet);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if(result)
        {
            sendMessage.setText("Гроші успішно знято");
        }
        else
        {
            sendMessage.setText("Недостатньо коштів");
        }

        try {
            execute(sendMessage);
            SendProfileInfo(chatId);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private void AddMoney(String currency,String amount, long chatId)
    {
        Optional<Wallet> walletOptional = walletsRepository.findById(chatId);
        Wallet wallet = walletOptional.get();
        double value = Double.parseDouble(amount);
        wallet.AddMoney(currency,value);
        walletsRepository.save(wallet);

        SendProfileInfo(chatId);
    }
    private void SendRateGraph(String currency, long chatId) throws IOException {
        ChartCreator chartCreator = new ChartCreator();
        chartCreator.GenerateChart(currency);
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        bCheckForAnswer=false;
        Currency=null;
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
    private void SendConvertedData(String message, long chatId,Update update)
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        switch (CurrentPage){
            case PROFILE_CURRENCY_TOP -> sendMessage.setText("Яку кількість ви хочете поповнити?");
            case PROFILE_CURRENCY_WITHDRAW -> sendMessage.setText("Яку кількість ви хочете зняти?");
            case CALCULATOR_CURRENCY -> sendMessage.setText("Яку кількість ви хочете обрахувати?");
            case SELL_CURRENCY -> sendMessage.setText("По якому курсу ви хочете продавати валюту?");
        }
        Currency=message;
        bCheckForAnswer=true;
       if(CurrentPage==Pages.GRAPHS_CURRENCY)
        {
            bCheckForAnswer = false;
            try {
                SendRateGraph(Currency,chatId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (CurrentPage==Pages.BUY_CURRENCY) {
           bCheckForAnswer = false;
            SendSellers(Currency,chatId);
           sendMessage.setText("Оберіть продавца:");
        }
        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void CalculateCurrency(String currency,String userMessage,long chatId ){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String answer = CurrencyCalculator.convertCurrency(Double.parseDouble(userMessage),currency);
        sendMessage.setText("Кол-во в "+currency+": "+answer);
        bCheckForAnswer=false;
        Currency=null;
        try {
            execute(sendMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void registerSeller(long chatId)
    {
        if(sellersRepository.findById(chatId).isEmpty()){
            Sellers seller = new Sellers();
            seller.setChatId(chatId);
            sellersRepository.save(seller);
        }
    }
    private void removeSeller(long chatId)
    {
        Optional<Sellers> optionalSeller = sellersRepository.findById(chatId);
        if (optionalSeller.isPresent()) {
            Sellers seller = optionalSeller.get();
            sellersRepository.delete(seller);
        }
    }

    private void SendSellers(String Currency,long chatId)
    {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();


        var sellers = sellersRepository.findAll();

        for(Sellers seller : sellers)
        {
            if(seller.getChatId()!=chatId && seller.getRate(Currency)>0.0) {
                var user = userRepository.findById(seller.getChatId());
                User userUser = user.get();
                String buttonText = userUser.getFirstName() + " - " + seller.getRate(Currency);
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(buttonText);
                button.setCallbackData(chatId + " - " + seller.getChatId());
                row.add(button);
                rows.add(row);
            }
        }

        inlineKeyboardMarkup.setKeyboard(rows);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Доступные продавцы:");
       sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void MakeTrade(long BuyerId, long SellerId,String Amount)
    {
        SendMessage sendMessage = new SendMessage();
        SendMessage notifyMessage = new SendMessage();
        sendMessage.setChatId(BuyerId);
        notifyMessage.setChatId(SellerId);
        String message="";

        var sellerOpt = sellersRepository.findById(SellerId);
        Sellers seller = sellerOpt.get();

        Double currencyAmount = Double.parseDouble(Amount);
        Double uahAmount = seller.getRate(Currency) * currencyAmount;

        var sellerWOpt = walletsRepository.findById(SellerId);
        Wallet sellerWallet = sellerWOpt.get();

        var buyerWOpt = walletsRepository.findById(BuyerId);
        Wallet buyerWallet = buyerWOpt.get();

        if(!sellerWallet.WithdrawMoney(Currency,currencyAmount))
        {
            message="У продавця недостатньо коштів";
            sendMessage.setText(message);
            try {
                execute(sendMessage);
                SendProfileInfo(BuyerId);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }
        if(!buyerWallet.WithdrawMoney("UAH",uahAmount))
        {
            message="У вас недостатньо коштів";
            sellerWallet.AddMoney(Currency,currencyAmount);
            sendMessage.setText(message);
            try {
                execute(sendMessage);
                SendProfileInfo(BuyerId);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            return;
        }

        buyerWallet.AddMoney(Currency,currencyAmount);
        sellerWallet.AddMoney("UAH",uahAmount);
        walletsRepository.save(buyerWallet);
        walletsRepository.save(sellerWallet);

        notifyMessage.setText("У вас купили валюту. Ви отримали - " + uahAmount + " UAH");
        message="Обмін виконано. Ви отримали - "+ currencyAmount+ " "+ Currency;
        sendMessage.setText(message);
        try {
            execute(sendMessage);
            execute(notifyMessage);
            SendProfileInfo(BuyerId);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
    private void SetTradeRate(long chatId,String value)
    {
        var sellerOpt = sellersRepository.findById(chatId);
        Sellers seller = sellerOpt.get();
        Double doubleValue =Double.parseDouble(value);
        seller.setRate(Currency,doubleValue);
        sellersRepository.save(seller);
        String message = "Ви успішно виставили валюту на продаж. "+Currency+ " по курсу - " +value;
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        CurrentPage=Pages.MARKETPLACE;
        ShowMarketplaceOptions("",chatId);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}