package ua.in.quireg.primenumberscalculation;


import android.app.Activity;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import ua.in.quireg.primenumberscalculation.models.IntervalModel;

public class Utils {

    public static List<IntervalModel> parseXMLfromAssets(Activity activity) throws IOException {
        AssetManager am = activity.getAssets();
        InputStream is = am.open("input.xml");

        ArrayList<IntervalModel> models = new ArrayList<>();

        Scanner s = new Scanner(is);
        try {
            while (s.hasNext()) {
                s.nextLine();

                if (s.nextLine().trim().equals("<root>")) {

                    if (s.nextLine().trim().equals("<intervals>")) {

                        boolean intervalsLoopFlag = true;

                        while (intervalsLoopFlag) {

                            int id;
                            int low;
                            int high;

                            if (s.nextLine().trim().equals("<interval>")) {

                                try{
                                    id = Integer.parseInt(s.nextLine().replace("<id>", "").replace("</id>", "").trim());
                                }catch (NumberFormatException e){
                                    throw new UnsupportedEncodingException();
                                }
                                try{
                                    low = Integer.parseInt(s.nextLine().replace("<low>", "").replace("</low>", "").trim());
                                }catch (NumberFormatException e){
                                    throw new UnsupportedEncodingException();
                                }
                                try{
                                    high = Integer.parseInt(s.nextLine().replace("<high>", "").replace("</high>", "").trim());
                                }catch (NumberFormatException e){
                                    throw new UnsupportedEncodingException();
                                }

                                if (!s.nextLine().trim().equals("</interval>")) {
                                    throw new UnsupportedEncodingException();
                                }

                                models.add(new IntervalModel(id, low, high));

                            } else {

                                intervalsLoopFlag = false;

                                //not <interval>, assuming it was </intervals>

                                if (!s.nextLine().trim().equals("</root>")) {
                                    throw new UnsupportedEncodingException();
                                }
                            }
                        }
                    } else {
                        throw new UnsupportedEncodingException();
                    }
                } else {
                    throw new UnsupportedEncodingException();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            is.close();
        }

        return models;
    }
}
