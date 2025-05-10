import Link from "next/link";
import styles from "../page.module.css";
import { RegForm } from "@/components/auth_forms";
import FetchUserSession from "@/components/fetch_user_session";
import LoadingPanel from "@/components/loading_panel";

export default function Register() {
return (
    <>
      <LoadingPanel ignore_error={true}></LoadingPanel>
      <main className={styles.main}>
      <FetchUserSession mustHaveSession={false} redirect={"/"}></FetchUserSession>
      <div className={styles.form_block}>
        <h1>Регистрация</h1>
        <RegForm></RegForm>
        <Link href={"/login"}>У меня уже есть аккаунт</Link>
      </div>
    </main>
    </>
  );
}