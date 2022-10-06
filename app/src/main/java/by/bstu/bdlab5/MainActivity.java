package by.bstu.bdlab5;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;

public class MainActivity extends AppCompatActivity {

    public String fileName = "Lab.txt";
    public File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isFileExists();
    }

    private boolean isFileExists(){
        File f = new File(super.getFilesDir(), fileName);
        boolean re = false;
        if(f.exists()){
            Log.d("Log4", "Файл " + fileName + " существует");
            Toast toast = Toast.makeText(this, "Файл " + fileName + " существует", Toast.LENGTH_LONG);
            toast.show();
            re = true;
            file = f;
        }
        else{
            Log.d("Log4", "Файл " + fileName + " не найден");
            Toast toast = Toast.makeText(this, "Файл " + fileName + " не найден и будет создан", Toast.LENGTH_LONG);
            toast.show();
            re = false;
            try{
                f.createNewFile();
                Log.d("Log4", "Файл " + fileName + " создан");
                file = f;
            }
            catch (Exception ex){
                Log.d("Log4", "Файл " + fileName + " не создан");
            }
        }
        return re;
    }

    int firstDigit(int number){
        if(number / 10 == 0){
            return number;
        }
        return firstDigit(number / 10);
    }

    int getNumberHash(String input){
        int hash = input.hashCode();
        int output = firstDigit(hash);
        if(output < 0){
            output *= -1;
        }
        return output;
    }


    public void saveValue(View view) {
        try{
            EditText keyInput = findViewById(R.id.keyInput);
            EditText valueInput = findViewById(R.id.valueInput);

            String value = String.valueOf(valueInput.getText());
            String key = String.valueOf(keyInput.getText());


            WriteTable(key, value);
        }
        catch (Exception ex){
            Log.d("LAB5", ex.getMessage());
        }

    }

    /*public int isValueExists(int hash){
        int existPosition = -1;
        try{
            RandomAccessFile raf = new RandomAccessFile(file, "r");
            for(int i = 0; i < 100; i+=11){
                raf.seek(i);
                int readHash = raf.read() - 48;
                Log.d("LAB5", String.valueOf(i + "   readHash = " + readHash));
                if(readHash == hash){
                    existPosition = i;
                    break;
                }
            }
            raf.close();
        }
        catch (Exception ex){
            Log.d("LAB5", ex.getMessage());
        }
        return existPosition;
    }*/

    public String ReadTable(String key){
        while(key.length() != 5){
            key += "#";
        }
        String value = "not found";
        try{
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            int readHash = 0;
            int hash = getNumberHash(key);
            int position = 0;
            do{
                raf.seek(position);
                readHash = raf.read() - 48;
                if(!(readHash == hash)){
                    position = position + 21;
                }
                else{
                    byte[] readKey = new byte[5];
                    raf.read(readKey);
                    String readKeyStr = new String(readKey);
                    if(readKeyStr.equals(key)){
                        byte[] readValue = new byte[10];
                        raf.read(readValue);
                        value = new String(readValue);
                        break;
                    }
                    else{
                        raf.skipBytes(10);
                        byte[] readLink = new byte[5];
                        raf.read(readLink);
                        String readLinkStr = new String(readLink);
                        if(readLinkStr.equals("@@@@@")){
                            value = "not found";
                            break;
                        }
                        else{
                            readLinkStr = readLinkStr.replace("@", "");
                            position = Integer.parseInt(readLinkStr);
                        }
                    }
                }
                Log.d("LAB5", String.valueOf(readHash));
            }
            while(readHash != -49);
            raf.close();
            String log;
            if(value.equals("not found")){
                log = "Хеш: " + hash + "\n Поиск выполнен. Значение не найдено.";
            }
            else{
                log = "Хеш: " + hash + "\n Поиск выполнен.";
            }
            Toast toast = Toast.makeText(this, log, Toast.LENGTH_LONG);
            toast.show();
        }
        catch(Exception ex){
            Log.d("LAB5", ex.getMessage());
        }
        value = value.replace("#", "");
        return value;
    }

    public void WriteTable(String key, String value){
        String log = "";
        try{
            while(value.length() != 10){
                value += "#";
            }
            while(key.length() != 5){
                key += "#";
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            int readHash = 0;
            int hash = getNumberHash(key);
            int position = 0;
            readHash = raf.read() - 48;
            Log.d("LAB55", String.valueOf(readHash));
                do{
                    raf.seek(position);
                    readHash = raf.read() - 48;
                    if(readHash == -49){
                        raf.seek(raf.length());
                        raf.writeBytes(String.valueOf(hash) + key + value + "@@@@@");
                        log = "Данные записаны в конец файла.";
                    }
                    if(!(readHash == hash)){
                        position = position + 21;
                    }
                    else{
                        byte[] readKey = new byte[5];
                        raf.read(readKey);
                        String readKeyStr = new String(readKey);
                        if(readKeyStr.equals(key)){
                            raf.writeBytes(value);
                            log = "Найдено соответствие по ключу. Данные перезаписаны.";
                            break;
                        }
                        else{
                            raf.skipBytes(10);
                            byte[] readLink = new byte[5];
                            raf.read(readLink);
                            String readLinkStr = new String(readLink);
                            if(readLinkStr.equals("@@@@@")){
                                long link = raf.length();
                                String linkStr = String.valueOf(link);
                                while(linkStr.length() != 5){
                                    linkStr += "@";
                                }
                                raf.seek(raf.getFilePointer() - 5);
                                raf.writeBytes(linkStr);
                                raf.seek(raf.length());
                                String message = String.valueOf(hash) + key + value + "@@@@@";
                                raf.writeBytes(message);
                                log = "Соответсвий по ключу не найдено. " +
                                        "Данные записаны в конец файла. " +
                                        "Ссылка записана в предыдущий схожий по хешу элемент.";
                                break;
                            }
                            else{
                                readLinkStr = readLinkStr.replace("@", "");
                                position = Integer.parseInt(readLinkStr);
                            }
                        }
                    }
                    Log.d("LAB5", String.valueOf(readHash));
                }
                while(readHash != -49);

            raf.close();

            Toast toast = Toast.makeText(this, log, Toast.LENGTH_LONG);
            toast.show();
        }
        catch(Exception ex){
            Log.d("LAB5", ex.getMessage());
        }

    }

    /*public void WriteFile(String message, int position){
        try{
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            raf.seek(raf.length());
            raf.writeBytes(message);
            raf.close();
        }
        catch (Exception ex){
            Log.d("LAB5", ex.getMessage());
        }

    }*/

    /*
    * byte[] bytes = "hello world".getBytes();

//Convert byte[] to String
String s = new String(bytes);*/

    public void getValue(View view) {
        EditText keyInputToGet = findViewById(R.id.keyInputToGet);
        String key = String.valueOf(keyInputToGet.getText());
        String value = ReadTable(key);

        EditText valueOutput = findViewById(R.id.valueOutput);
        valueOutput.setText(value);
    }
}