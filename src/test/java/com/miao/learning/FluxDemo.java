package com.miao.learning;
import reactor.core.publisher.Flux;




public class FluxDemo {
    public static void main(String[] args) {

        // 1. 【创建】丢进水管
        Flux<String> pipeline = Flux.just("apple", "banana", "orange", "grape");

        // 2. 【操作】在水管里装各种滤网和转换器
        pipeline
                .filter(fruit -> fruit.length() > 5)  // 过滤：只要长度大于 5 的（apple 会被刷掉）
                .map(String::toUpperCase)             // 转换：全部变大写
                .map(fruit -> "Fruits: " + fruit)     // 转换：加个前缀

                // 3. 【执行】接上水龙头，水才开始流
                .subscribe(
                        item -> System.out.println("拿到一个: " + item), // 处理每一个元素
                        err -> System.err.println("漏水了: " + err),      // 处理错误
                        () -> System.out.println("水流干了，关闸！")      // 完成信号
                );
    }
}

