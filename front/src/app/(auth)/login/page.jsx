import Link from "next/link";
import styles from "../page.module.css";
import { LoginForm } from "@/components/auth_forms";
import FetchUserSession from "@/components/fetch_user_session";
import LoadingPanel from "@/components/loading_panel";

export default function Login() {
return (
    <>
      <LoadingPanel ignore_error={true}></LoadingPanel>
      <main className={styles.main}>
      <FetchUserSession mustHaveSession={false} redirect={"/"}></FetchUserSession>
        <div className={styles.form_block}>
          <h1>Вход в аккаунт</h1>
          <LoginForm></LoginForm>
          <Link href={"/register"}>У меня нет аккаунта</Link>
        </div>
      </main>
    </>
  );
}