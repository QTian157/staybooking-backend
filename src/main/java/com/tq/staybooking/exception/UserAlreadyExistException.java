package com.tq.staybooking.exception;

/**
 * How could you deal with duplicate usernames?
 * We need to throw an exception when a user tries to reuse an existing username.
 * So let’s create a new package called com.tq.staybooking.exception
 * and add a new class called UserAlreadyExistException into it.
 */

// 💡 为什么要继承 RuntimeException？
    //-> RuntimeException 是 Java 里的“运行时异常”
    //-> 你抛出这种异常时，不需要强制写 try/catch（更适合业务错误处理）
    //-> Spring Boot 也习惯用 RuntimeException 来做业务层异常（比如用户名重复）
public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException(String message){
        super(message);
    }
}

// 这是这个类的“初始化方法”，当你要“抛出这个异常”时，会写：throw new UserAlreadyExistException("Username already exists.");
    // -> message 是你传入的错误信息
    // -> super(message) 是把这个错误信息传给 RuntimeException 的构造方法
    // -> 父类 RuntimeException 负责管理错误信息，你只需要把 message 传给它。
// 🌟 这个异常怎么用？（结合 registerService.add()）
    //public void add(User user, UserRole role) {
    //    if (userRepository.existsById(user.getUsername())) {
    //        throw new UserAlreadyExistException("Username already exists.");
    //    }
    //
    //    // 否则就继续创建用户
    //}


