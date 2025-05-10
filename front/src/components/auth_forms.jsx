"use client"

import { useForm } from "react-hook-form";
import styles from "./modules/auth_forms.module.css";
import { useRouter } from "next/navigation";

export function RegForm() {
    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm();
    const router = useRouter();

    const onSubmit = async (data) => {
        if (data.password1 === data.password2){
            try {
                const res = await fetch("http://localhost:8080/register", {
                    method: "POST",
                    credentials: "include",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({name: data.name, username: data.username, password: data.password1}),
                });
    
                if (!res.ok){
                    const error = await res.json();
                    setError("fail", {
                        type: "manual",
                        message: error.error
                    });
                } else {
                    router.push("/login");
                }
            } catch (e) {
                setError("fail", {
                    type: "manual",
                    message: "Ошибка регистрации. Попробуйте позже"
                });
            }
        } else {
            setError("passuneq", {
                type: "manual",
                message: "Пароли не совпадают"
            });
        }
    }

  return (
        <form onSubmit={handleSubmit(onSubmit)} className={styles.reg_form}>
            <div className={styles.input_wrapper}><input
                className={styles.field}
                type="text"
                placeholder="Никнейм"
                maxLength={20}
                {...register("name", { required: "Введите никнейм", onChange: () => clearErrors(["fail"]) })}
                disabled={isSubmitting}/>
                <p className={styles.error_msg}>{errors.nickname?.message ? errors.nickname?.message : "" }</p>
            </div>

            <div className={styles.input_wrapper}><input
                className={styles.field}
                type="text"
                placeholder="Имя пользователя"
                maxLength={20}
                {...register("username", { required: "Введите имя пользователя", onChange: () => clearErrors(["fail"]) })}
                disabled={isSubmitting}/>
                <p className={styles.error_msg}>{errors.username?.message ? errors.username?.message : "" }</p>
            </div>

            <div className={styles.input_wrapper}><input
                className={styles.field}
                type="password"
                placeholder="Пароль"
                maxLength={25}
                {...register("password1", { required: "Введите пароль", onChange: () => clearErrors(["fail", "passuneq"]) })}
                disabled={isSubmitting}/>
                <p className={styles.error_msg}>{errors.passuneq?.message ? errors.passuneq?.message : (errors.password1?.message ? errors.password1?.message : "")}</p>
            </div>

            <div className={styles.input_wrapper}><input
                className={styles.field}
                type="password"
                placeholder="Повтор пароля"
                {...register("password2", { required: "Повторите пароль", onChange: () => clearErrors(["fail", "passuneq"]) })}
                disabled={isSubmitting}/>
                <p className={styles.error_msg}>{errors.passuneq?.message ? errors.passuneq?.message : (errors.password2?.message ? errors.password2?.message : "")}</p>
            </div>
            
            <p className={styles.serv_error_msg}>{errors.fail?.message ? errors.fail?.message : "" }</p>
            <input type="submit" className={styles.button} disabled={isSubmitting} value={"Зарегистрироваться"}/>
        </form>
    );
}

export function LoginForm() {
    const {
        register,
        handleSubmit,
        setError,
        clearErrors,
        formState: { errors, isSubmitting },
    } = useForm();

    const router = useRouter();

    const onSubmit = async (data) => {
        try {
            const res = await fetch("http://localhost:8080/login", {
                method: "POST",
                credentials: "include",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data),
            });
            if (!res.ok){
                const error = await res.json();
                setError("fail", {
                    type: "manual",
                    message: error.error
                });
            } else {
                router.push("/");
            }
        } catch (e){
            setError("fail", {
                type: "manual",
                message: "Ошибка входа. Попробуйте позже"
            });
        }
    };

    return (
        <form onSubmit={handleSubmit(onSubmit)} className={styles.login_form}>
            <div className={styles.input_wrapper}><input
                className={styles.field}
                type="text"
                placeholder="Имя пользователя"
                maxLength={20}
                {...register("username", { required: "Введите имя пользователя", onChange: () => clearErrors(["fail"]) })}
                disabled={isSubmitting}/>
                <p className={styles.error_msg}>{errors.username?.message ? errors.username?.message : "" }</p>
            </div>

            <div className={styles.input_wrapper}><input
                className={styles.field}
                type="password"
                placeholder="Пароль"
                maxLength={25}
                {...register("password", { required: "Введите пароль", onChange: () => clearErrors(["fail"]) })}
                disabled={isSubmitting}/>
                <p className={styles.error_msg}>{errors.password?.message ? errors.password?.message : "" }</p>
            </div>
            <p className={styles.serv_error_msg}>{errors.fail?.message ? errors.fail?.message : "" }</p>
            <input type="submit" className={styles.button} disabled={isSubmitting} value={"Войти"}/>
        </form>
    );
}