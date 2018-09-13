package com.lyc.maven.entity;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.text.DecimalFormat;
import java.util.*;

//修改点：1，好友通讯录必须去重，否则会有weight>2的情况。2，Set<Integer> value1需要是临时变量，否则会影响到整个m1
@Slf4j
public class Common_friends_weight {
    private  List<Goods> goodsList=Lists.newArrayList(); //存放数据库搜索结果

    public void init() {
        Neo4jConnection testdb = new Neo4jConnection("localhost", "port", "neo4j", "password");//link to database
        String cypherProperties = "match (m:person)-[r2:`friend`]-(n:person)  return id(n),id(m) limit 1000 ";//query

        String resultProperties = testdb.exectCypherEx(cypherProperties);// get result
        JSONObject jsonObject = JSONObject.parseObject(resultProperties);//获取json对象

        JSONArray dataArray = jsonObject.getJSONArray("data");//获取json数组
        for (Object o : dataArray) {

            JSONArray array = (JSONArray) o;

            int n = array.getInteger(0);//第一列是n，作为手机机主看待
            int m = array.getInteger(1);//第二列是m，作为该手机的通讯录成员之一
            /*goodsList.add(Goods.builder().n(n).m(m).build());*/
            Goods goods = new Goods(n, m);
            goods.setN(n);
            goods.setM(m);
            goodsList.add(goods);//依次把n和m赋值到arraylist，此时人员之间是11对应关系
        }
        /*System.out.println(goodsList);*/

        Map<Integer, Set<Integer>> m1 = new HashMap<>();//x新建一个map，key是手机机主id，value是其通讯录的所有人id，set去重

        for (Goods goods : goodsList) {
            int key = goods.getN();
            int value = goods.getM();
            Set<Integer> ccccc = m1.getOrDefault(key, new HashSet<>());//新建一个临时array，用于保存key=n时对应的所有value
            ccccc.add(value);
            if (!m1.containsKey(key)) {//当m1中不存在该key时，也就是有新的key和value时，将新的值put到m1中
                m1.put(key, ccccc);
            }
        }
        System.out.println(m1);

       // HashMap<String, Integer> nummap = new HashMap <>();
        //相同好友map，人员1_人员2，相同好友个数

        DecimalFormat df = new DecimalFormat("0.00");//浮点化

        for (int key1 : m1.keySet()) {
            for (int key2 : m1.keySet()) {//在人员-通讯录map中两两人员进行遍历
                if (key2 != key1) {
                    Set<Integer> value1= new HashSet<>(m1.get(key1));
                    Set<Integer> value2= new HashSet<>(m1.get(key2));
                    Set<Integer> friendSet = new HashSet<>();
                    //将两人的通讯录进行分别去重，并新建一个set预备存放两者并集

                    friendSet.addAll(value1);
                    friendSet.addAll(value2);
                    //求并集

                    int size1=value1.size();
                    int size2=value2.size();
                    int size3=friendSet.size();
                    int sameFriendNum = size1+size2-size3;
                    //分别求size，两者和-并集=交集

                    String  weight=df.format((float)sameFriendNum/size3);
                    //weight=交集/并集

                    if (sameFriendNum>1){
                        System.out.println(key1+"_"+key2+":"+weight);
                    }
                    if (weight.compareTo("1.00")>0){
                        System.err.println("weight .......");
                    }
                }
            }
        }
        //System.out.println(nummap);



    }

    @Test
    public void test(){
        init();

    }
}


