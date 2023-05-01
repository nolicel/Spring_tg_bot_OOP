       telegramSpringBot_1.1
********************************
config
    //классы конфигурации и инициализации бота


model
    //сущности и репозитории которые взаимодействуют с бд не забываем что мы юзаем либу JPA
    //она автогенерит таблицы по сущностям нужно только не забывать юзать @анотации

service
    //по сути контроллеры нашего бота логбек это моя попытка реализовать логи у меня не вышло потом уберу

springBotApplication //ну наш мейн класс отсюда запускаем бота

application.properties {


            bot.name = exchange_212_bot //бот нейм отсюда подсасываются эти данные в конфигурационные класы
            bot.token = 6151276139:AAEhLJHfCJp13VWwZcYvOAtfrhqEk0mwFK4 //токен бота такая же история как и выше


# db related settings // крч если хочешь запустить у себя на компе редачь этот файл иначе не сбилдится
spring.jpa.hibernate.ddl-auto=update //приколюха для гибернейта и jpa если он не находит таблицу
                                     //то просто её создаёт
spring.datasource.url=jdbc:mysql://localhost:3306/tgbot // поднимешь датабазу на sql будет хорошо если найдешь
                                 //какую-то серверную чтоб там разместить датабазу названия бд tgbot
spring.datasource.username=root  // по дефолту
spring.datasource.password=Minimachine1 // тот который ты указал
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.show-sql=true

}