import SwiftUI

struct LoginView: View {
    @ObservedObject var viewModel: AuthViewModel
    @State private var username = ""
    @State private var password = ""
    @State private var showRegister = false
    
    var body: some View {
        NavigationStack {
            VStack(spacing: 24) {
                Spacer()
                
                // Logo
                VStack(spacing: 8) {
                    Text("欢迎回来")
                        .font(.largeTitle)
                        .fontWeight(.bold)
                        .foregroundColor(.primaryGreen)
                    Text("登录以继续使用易物")
                        .foregroundColor(.gray)
                }
                
                Spacer()
                
                // Input fields
                VStack(spacing: 16) {
                    TextField("用户名", text: $username)
                        .textFieldStyle(.roundedBorder)
                        .autocapitalization(.none)
                        .autocorrectionDisabled()
                    
                    SecureField("密码", text: $password)
                        .textFieldStyle(.roundedBorder)
                }
                .padding(.horizontal)
                
                // Error message
                if let error = viewModel.error {
                    Text(error)
                        .foregroundColor(.red)
                        .font(.caption)
                }
                
                // Login button
                Button(action: {
                    Task {
                        await viewModel.login(username: username, password: password)
                    }
                }) {
                    if viewModel.isLoading {
                        ProgressView()
                            .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    } else {
                        Text("登录")
                    }
                }
                .frame(maxWidth: .infinity)
                .padding()
                .background(Color.primaryGreen)
                .foregroundColor(.white)
                .cornerRadius(12)
                .padding(.horizontal)
                .disabled(username.isEmpty || password.isEmpty || viewModel.isLoading)
                
                // Register link
                Button("还没有账号？立即注册") {
                    showRegister = true
                }
                .foregroundColor(.primaryGreen)
                
                Spacer()
            }
            .navigationDestination(isPresented: $showRegister) {
                RegisterView(viewModel: viewModel)
            }
        }
    }
}

struct RegisterView: View {
    @ObservedObject var viewModel: AuthViewModel
    @Environment(\.dismiss) private var dismiss
    
    @State private var username = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var nickname = ""
    
    var body: some View {
        VStack(spacing: 24) {
            // Header
            VStack(spacing: 8) {
                Text("创建账号")
                    .font(.largeTitle)
                    .fontWeight(.bold)
                    .foregroundColor(.primaryGreen)
                Text("注册以开始使用易物")
                    .foregroundColor(.gray)
            }
            .padding(.top, 32)
            
            // Input fields
            VStack(spacing: 16) {
                TextField("用户名", text: $username)
                    .textFieldStyle(.roundedBorder)
                    .autocapitalization(.none)
                    .autocorrectionDisabled()
                
                TextField("邮箱", text: $email)
                    .textFieldStyle(.roundedBorder)
                    .autocapitalization(.none)
                    .keyboardType(.emailAddress)
                
                TextField("昵称（选填）", text: $nickname)
                    .textFieldStyle(.roundedBorder)
                
                SecureField("密码", text: $password)
                    .textFieldStyle(.roundedBorder)
                
                SecureField("确认密码", text: $confirmPassword)
                    .textFieldStyle(.roundedBorder)
            }
            .padding(.horizontal)
            
            // Error message
            if let error = viewModel.error {
                Text(error)
                    .foregroundColor(.red)
                    .font(.caption)
            }
            
            if password != confirmPassword && !confirmPassword.isEmpty {
                Text("两次输入的密码不一致")
                    .foregroundColor(.red)
                    .font(.caption)
            }
            
            // Register button
            Button(action: {
                Task {
                    await viewModel.register(
                        username: username,
                        email: email,
                        password: password,
                        nickname: nickname.isEmpty ? nil : nickname
                    )
                }
            }) {
                if viewModel.isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                } else {
                    Text("注册")
                }
            }
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color.primaryGreen)
            .foregroundColor(.white)
            .cornerRadius(12)
            .padding(.horizontal)
            .disabled(!isFormValid || viewModel.isLoading)
            
            Spacer()
        }
        .navigationBarTitleDisplayMode(.inline)
    }
    
    private var isFormValid: Bool {
        !username.isEmpty &&
        !email.isEmpty &&
        !password.isEmpty &&
        password == confirmPassword &&
        password.count >= 6
    }
}
