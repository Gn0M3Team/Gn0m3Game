//package com.gnome.gnome.aspects;
//
//import com.gnome.gnome.db.DatabaseWrapper;
//import com.gnome.gnome.editor.controller.EditorPageController;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//
//import java.util.logging.Logger;
//
//
///**
// * Aspect that handles methods annotated with {@link com.gnome.gnome.annotations.db.Transactional}.
// * <p>
// * Wraps the method in manual transaction management:
// * <ul>
// *     <li>Starts a transaction before method execution</li>
// *     <li>Commits the transaction if no exception occurs</li>
// *     <li>Rolls back the transaction if an exception is thrown</li>
// * </ul>
// */
//@Aspect
//public class TransactionalAspect {
//
//
//    // Singleton instance of the database wrapper.
//    private static DatabaseWrapper db;
//
//    // Logger for logging transaction events and errors.
//    private static final Logger logger = Logger.getLogger(TransactionalAspect.class.getName());
//
//    /**
//     * Constructor that initializes the database wrapper instance.
//     */
//    public TransactionalAspect() {
//        db = DatabaseWrapper.getInstance();
//    }
//
//    /**
//     * Around advice that wraps a method annotated with @Transactional in a transaction.
//     *
//     * @param joinPoint the method being executed
//     * @return the result of the method
//     * @throws Throwable any exception thrown by the method
//     */
//    @Around("@annotation(com.gnome.gnome.annotations.db.Transactional)")
//    public Object transactionSQL(ProceedingJoinPoint joinPoint) throws Throwable {
//        logger.info("Transaction started");
//
//        Object result = null;
//        try {
//            // Begin a new transaction before method execution.
//            db.beginTransaction();
//
//            // Proceed with the intercepted method.
//            result = joinPoint.proceed();
//
//            // If no exception, commit the transaction.
//            db.commitTransaction();
//        } catch (Throwable t) {
//            // Roll back the transaction on any error.
//            db.rollBackTransaction();
//            logger.severe("Exception occurred inside transactional method: " + t.getMessage());
//            // Rethrow the exception to let callers handle it.
//            throw t;
//        }
//
//        return result;
//    }
//}
