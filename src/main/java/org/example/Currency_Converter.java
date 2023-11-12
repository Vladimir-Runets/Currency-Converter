package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

public class Currency_Converter extends JFrame {

    private final JButton convertButton;
    private final DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
    private final JLabel amountLabel, fromLabel, toLabel, resultLabel;
    private final JTextField amountField;
    private final JComboBox<String> fromComboBox, toComboBox;
    private final String[] currencies = {"USD","EUR","RUB"};
    public Currency_Converter() {
        setTitle("Currency Converter of Belarusbank");
        setLayout(new GridLayout(4, 2));

        amountLabel = new JLabel("Amount:");
        add(amountLabel);
        amountField = new JTextField();
        add(amountField);
        fromLabel = new JLabel("From:");
        add(fromLabel);
        fromComboBox = new JComboBox<>(currencies);
        add(fromComboBox);
        toLabel = new JLabel("To:");
        add(toLabel);
        toComboBox = new JComboBox<>(currencies);
        add(toComboBox);
        convertButton = new JButton("Convert");
        add(convertButton);
        resultLabel = new JLabel();
        add(resultLabel);

        String responseBody = "", str = "";

        // Формирование URL-адреса запроса
        String apiUrl = "https://belarusbank.by/api/kursExchange?city=Лида";

        // Создание HTTP-клиента
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            // Создание GET-запроса
            HttpGet httpGet = new HttpGet(apiUrl);

            // Выполнение запроса
            HttpResponse response = httpClient.execute(httpGet);

            // Получение ответа в виде HTTP-сущности
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                // Преобразование HTTP-сущности в строку
                responseBody = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Pattern patternToFindDescription = Pattern.compile("\"CHF_out\":\"(.*?),\"(.*?)\",\"JPY_in\"");
        Matcher matcherToFindDescription = patternToFindDescription.matcher(responseBody);
        if (matcherToFindDescription.find()) {
            str = matcherToFindDescription.group(2);
        }

        // Разбиение строки на пары ключ-значение
        String[] pairs = str.split("\",\"");

        // Создание HashMap для хранения ключей и значений
        HashMap<String, Double> conversion = new HashMap<>();

        // Разбор и сохранение пар ключ-значение в HashMap
        for (String pair : pairs) {
            String[] keyValue = pair.split("\":\"");
            String key = keyValue[0];
            if (key.endsWith("_out")) {
                key = key.replace("_out", "");
                key = key.substring(4) + "_" + key.substring(0,3);
            } else {
                key = key.replace("_in", "");
            }
            double value = Double.parseDouble(keyValue[1]);
            conversion.put(key, value);
        }

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    double amount = Double.parseDouble(amountField.getText());
                    String fromCurrency = (String) fromComboBox.getSelectedItem();
                    String toCurrency = (String) toComboBox.getSelectedItem();
                    double result;
                    if (fromCurrency.equals(toCurrency)){ result = amount; }
                    else { result = amount * conversion.get(fromCurrency + "_" + toCurrency); }
                    resultLabel.setText(decimalFormat.format(result) + " " + toCurrency);
                } catch (Exception ex) {
                    resultLabel.setText("Invalid input");
                }
            }
        });

        setSize(400, 300);
        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public static void main(String[] args) {
        new Currency_Converter();
    }
}