package com.yunjisuan.presentation.controller;

import com.yunjisuan.presentation.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DataController {
    private final MongoTemplate mongoTemplate;

    public DataController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @RequestMapping(value = "/eduCount", method = RequestMethod.GET)
    public List<EducationCount> getEduCountData() {
        return mongoTemplate.findAll(EducationCount.class);
    }

    @RequestMapping(value = "/jobCount", method = RequestMethod.GET)
    public List<JobCount> getJobCountData() {
        return mongoTemplate.findAll(JobCount.class);
    }

    @RequestMapping(value = "/jobClassCount", method = RequestMethod.GET)
    public List<JobClassCount> getJobClassCountData() {
        return mongoTemplate.find(new Query().with(Sort.by(Sort.Direction.DESC, "count")), JobClassCount.class);
    }

    @RequestMapping(value = "/cityCount", method = RequestMethod.GET)
    public List<CityCount> getCityCountData() {
        return mongoTemplate.find(new Query().with(Sort.by(Sort.Direction.DESC, "count")).limit(10), CityCount.class);
    }

    @RequestMapping(value = "/handle", method = RequestMethod.GET)
    public void handle() {
        List<CityCount> data = mongoTemplate.findAll(CityCount.class);
        mongoTemplate.remove(Query.query(Criteria.where("type").all()), CityCount.class);
        for (CityCount cityCount : data) {
            String code = cityCount.getType();
            String procode = code.substring(0, 2);
            String citycode = code.substring(0, 4);
            System.out.println(code);
            CityCode cityCode = mongoTemplate.findOne(Query.query(Criteria.where("code").is(procode)), CityCode.class);

            String pro = cityCode.getPro();
            System.out.println(pro);
            String city = "";
            for (Child child : cityCode.getChildren()) {
                if (child.getCode().length() != 4) {
                    city = pro;
                    break;
                }
                if (child.getCode().equals(citycode)) {
                    city = child.getName();
                    break;
                }
            }
            cityCount.setPro(pro);
            cityCount.setCity(city);
            cityCount.setId(null);
        }
        mongoTemplate.insert(data, CityCount.class);
    }
}
