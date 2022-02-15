let btnLogOut = document.getElementById('logoutBtn');
if (btnLogOut !== null) {
  btnLogOut.onclick = () => {
    Vue.$cookies.remove('TokenJWT');

    try {
      let auth2 = gapi.auth2.getAuthInstance();
      auth2.signOut().then(function () {
        console.log('User signed out.');
        window.location.href = './index.html';
      });
    } catch (error) {
      window.location.href = './index.html';
    }

  }
}
