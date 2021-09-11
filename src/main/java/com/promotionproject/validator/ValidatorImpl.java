package com.promotionproject.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

/***
 * 和之前的"包装器业务异常类实现"相似：
 * 1，定义一个validation
 */
@Component
public class ValidatorImpl implements InitializingBean {
    private Validator validator;
    @Override
    public void afterPropertiesSet() throws Exception {
        //通过工厂初始化方式使其实例化：
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }
    /**
     * 实现校验方法，并返回校验结果：
     */
    public ValidationResult validate(Object bean){
        final ValidationResult validationResult = new ValidationResult();
        //通过validator进行校验：
        Set<ConstraintViolation<Object>> constraintViolations = this.validator.validate(bean);
        if(constraintViolations.size()>0){
            //有错误：
            validationResult.setHasErrors(true);
            constraintViolations.forEach(constraintViolation->{
                //遍历：
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                validationResult.getErrorMsg().put(propertyName, errMsg);
            });
        }
        return validationResult;
    }
    //如何使用它：在哪里定义规则，使得this.validator.validate(bean)能够有效地检验出结果:
    //在被检验的对象的类中对需要特殊要求的属性加上相应的注解：
    //@NotBlank(message = "xxxx"): 不能为""或者null，不然就会报错，可以自定义报错信息
    //等。。。。见usermodel.java
    //当把usermodel.java的属性都制定好规则后，就可以在需要对usermodel对象进行校验null和空值的地方：
    //直接：注入ValidatorImpl这个bean即可，然后调用上边的：
    // public ValidationResult validate(Object bean){ 方法。


}
